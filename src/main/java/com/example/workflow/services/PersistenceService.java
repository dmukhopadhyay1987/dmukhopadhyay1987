package com.example.workflow.services;

import com.example.feign.GitFeign;
import com.example.feign.model.BlobRequest;
import com.example.feign.model.BranchRequest;
import com.example.feign.model.BranchUpdateRequest;
import com.example.feign.model.CommitRequest;
import com.example.feign.model.MergeRequest;
import com.example.feign.model.TreeRequest;
import com.example.vo.Blob;
import com.example.vo.Branch;
import com.example.vo.Commit;
import com.example.vo.Reference;
import com.example.vo.Tree;
import com.example.vo.TreeDetail;
import com.example.workflow.model.ProcessInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@EnableCaching
public class PersistenceService {

	@Autowired
	private GitFeign gitClient;
	@Autowired
	private ObjectMapper objectMapper;
	@Autowired
	private BlobRequest blobRequest;
	@Autowired
	private BranchRequest branchRequest;
	@Autowired
	private TreeRequest treeRequest;
	@Autowired
	private CommitRequest commitRequest;
	@Autowired
	private BranchUpdateRequest branchUpdateRequest;
	@Autowired
	private MergeRequest mergeRequest;

	@Cacheable(cacheNames = "blobs", keyGenerator = "keyGen", sync = true)
	private Blob blob(String sha) {
		return gitClient.blob(sha).orElseThrow();
	}

	@Cacheable(cacheNames = "blobs", keyGenerator = "keyGen", sync = true)
	private Tree tree(String sha) {
		return gitClient.tree(sha).orElseThrow();
	}

	@Cacheable(cacheNames = "blobs", keyGenerator = "keyGen", sync = true)
	private Commit commit(String sha) {
		return gitClient.commit(sha).orElseThrow();
	}

	@Cacheable(cacheNames = "refs", keyGenerator = "keyGen", sync = true)
	private Optional<Reference> ref(String sha) {
		return gitClient.ref(sha);
	}

	public Reference branch(String branchName) {
		Reference main = ref("main").orElseThrow();
		branchRequest.setSha(main.getObject().getSha());
		branchRequest.setRef("refs/heads/" + branchName);
		return ref(branchName).orElseGet(() -> gitClient.createBranch(branchRequest));
	}

	@SneakyThrows
	public Commit save(ProcessInfo processInfo, String message) {
		String branchName = processInfo.getLoanNumber();
		Reference branch = branch(branchName);
		blobRequest.setContent(objectMapper.writeValueAsString(processInfo));
		Commit lastCommit = commit(branch.getObject().getSha());
		List<TreeDetail> modifiedBaseTrees = tree(lastCommit.getCommitDetails().getTree().getSha())
				.getTreeDetail()
				.stream().filter(t -> t.getType().equals("tree") && !t.getPath().equals(processInfo.getLoanNumber()))
				.collect(Collectors.toList());
		modifiedBaseTrees.add(new TreeDetail(gitClient.createBlob(blobRequest).getSha(),
				processInfo.getLoanNumber().concat("/").concat(processInfo.getClass().getSimpleName().concat(".json")),
				"blob",
				"100644"));
		treeRequest.setTree(modifiedBaseTrees);
		Tree tree = gitClient.createTree(treeRequest);

		commitRequest.setTree(tree.getSha());
		commitRequest.setMessage(message);
		commitRequest.setParents(List.of(lastCommit.getSha()));
		Commit commit = gitClient.createCommit(commitRequest);

		branchUpdateRequest.setSha(commit.getSha());
		gitClient.updateBranch(branch.getRef().replace("refs/heads/", ""), branchUpdateRequest);
		return commit;
	}

	@SneakyThrows
	public ProcessInfo get(String loanNumber, String sha) {
		return objectMapper.readValue(Base64.getDecoder().decode(
				blob(
						commit(sha).getFiles().stream()
								.filter(f -> f.getFilename().equals(loanNumber.concat("/").concat("ProcessInfo.json")))
								.findFirst().orElseThrow().getSha())
						.getBase64content()
						.replace("\n", "")
						.replace("\r", "")), ProcessInfo.class);
	}

	public void merge(String branchName, String message) {
		List<Branch> branches = gitClient.branches();
		String head = branches.stream()
				.filter(b -> b.getName().equals(branchName))
				.findFirst().orElseThrow()
				.getCommit()
				.getSha();
		String base = branches.stream()
				.filter(b -> b.getName().equals("main"))
				.findFirst().orElseThrow()
				.getName();
		mergeRequest.setBase(base);
		mergeRequest.setHead(head);
		mergeRequest.setCommitMessage(message);
		gitClient.merge(mergeRequest);
		gitClient.deleteBranch(branchName);
	}
}
