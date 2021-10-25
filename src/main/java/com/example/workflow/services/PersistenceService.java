package com.example.workflow.services;

import com.example.feign.GitFeign;
import com.example.feign.model.BlobRequest;
import com.example.feign.model.BranchRequest;
import com.example.feign.model.BranchUpdateRequest;
import com.example.feign.model.CommitRequest;
import com.example.feign.model.MergeRequest;
import com.example.feign.model.TreeRequest;
import com.example.vo.Branch;
import com.example.vo.Commit;
import com.example.vo.Tree;
import com.example.vo.TreeDetail;
import com.example.workflow.model.ProcessInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
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

	public Branch branch(String branchName) {
		List<Branch> branches = branches();
		if (branches.stream().noneMatch(b -> b.getName().equals(branchName))) {
			branchRequest.setRef("refs/heads/" + branchName);
			branchRequest.setSha(branches.stream().filter(b -> b.getName().equals("main")).findFirst().orElseThrow().getCommit().getSha());
			gitClient.createBranch(branchRequest);
			return branch(branchName);
		}
		return branches.stream().filter(b -> b.getName().equals(branchName)).findFirst().orElseThrow();
	}

	private List<Branch> branches() {
		return gitClient.branches();
	}

	@SneakyThrows
	public Commit save(ProcessInfo processInfo, String message) {
		String branchName = processInfo.getLoanNumber();
		Branch branch = branch(branchName);
		blobRequest.setContent(objectMapper.writeValueAsString(processInfo));
		Commit lastCommit = gitClient.commit(branch.getCommit().getSha());
		List<TreeDetail> modifiedBaseTrees = gitClient.tree(lastCommit.getCommitDetails().getTree().getSha())
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
		gitClient.updateBranch(branch.getName(), branchUpdateRequest);
		return commit;
	}

	@SneakyThrows
	public ProcessInfo get(String loanNumber, String sha) {
		return objectMapper.readValue(Base64.getDecoder().decode(
				gitClient.blob(
								gitClient.commit(sha).getFiles().stream()
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
	}
}
