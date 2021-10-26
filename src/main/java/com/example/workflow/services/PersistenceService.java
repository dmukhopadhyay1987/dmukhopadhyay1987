package com.example.workflow.services;

import com.example.feign.GitFeign;
import com.example.feign.model.BlobRequest;
import com.example.feign.model.BranchRequest;
import com.example.feign.model.BranchUpdateRequest;
import com.example.feign.model.CommitRequest;
import com.example.feign.model.MergeRequest;
import com.example.feign.model.TreeRequest;
import com.example.vo.Blob;
import com.example.vo.Commit;
import com.example.vo.File;
import com.example.vo.Reference;
import com.example.vo.Tree;
import com.example.vo.TreeDetail;
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
public class PersistenceService<T> {

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
	private Optional<Reference> ref(String ref) {
		return gitClient.ref(ref);
	}

	private Reference branch(String branchName) {
		Reference main = ref("main").orElseThrow();
		branchRequest.setSha(main.getObject().getSha());
		branchRequest.setRef("refs/heads/" + branchName);
		return ref(branchName).orElseGet(() -> gitClient.createBranch(branchRequest));
	}

	@SneakyThrows
	public Commit save(String branchName, String path, T payload, String message) {
		Reference branch = branch(branchName);
		blobRequest.setContent(objectMapper.writeValueAsString(payload));
		Commit lastCommit = commit(branch.getObject().getSha());
		List<TreeDetail> modifiedBaseTrees = tree(lastCommit.getCommitDetails().getTree().getSha())
				.getTreeDetail()
				.stream().filter(t -> t.getType().equals("tree") && !path.contains(t.getPath()))
				.collect(Collectors.toList());
		modifiedBaseTrees.add(new TreeDetail(gitClient.createBlob(blobRequest).getSha(),
				path,
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
	public T get(String path, String sha, Class<T> c) {
		return objectMapper.readValue(Base64.getDecoder().decode(
				blob(
						commit(sha).getFiles().stream()
								.filter(f -> f.getFilename().equals(path))
								.findFirst().orElseThrow().getSha())
						.getBase64content()
						.replace("\n", "")
						.replace("\r", "")), c);
	}

	public void merge(String branchName, String message) {
		String head = ref(branchName).orElseThrow()
				.getObject()
				.getSha();
		mergeRequest.setBase("main");
		mergeRequest.setHead(head);
		mergeRequest.setCommitMessage(message);
		gitClient.merge(mergeRequest);
		gitClient.deleteBranch(branchName);
	}

	public List<File> history(String path) {
		List<Commit> commitChain = chain(commit(ref("main").orElseThrow()
				.getObject()
				.getSha()), null);
		return commitChain.stream()
				.map(c -> c.getFiles()
						.stream().filter(f -> f.getFilename().equals(path))
						.findFirst().orElseThrow())
				.collect(Collectors.toList());
	}

	public List<Commit> chain(Commit head, List<Commit> list) {
		if (list == null) {
			list = List.of(head);
		}
		List<Commit> parents = head.getParents();
		for (Commit p: parents) {
			Commit parentCommit = commit(p.getSha());
			list.add(parentCommit);
			return chain(parentCommit, list);
		}
		return list;
	}
}
