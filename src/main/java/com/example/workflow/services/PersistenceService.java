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
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableCaching
public class PersistenceService<T> {

	public static final String TREE = "tree";
	public static final String BLOB = "blob";
	public static final String BLOB_MODE = "100644";
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

	@Cacheable(cacheNames = "blobs", keyGenerator = "keyGen")
	private Blob blob(String sha) {
		log.debug("GET blob {}", sha);
		return gitClient.blob(sha).orElseThrow();
	}

	@Cacheable(cacheNames = "tree", keyGenerator = "keyGen")
	private Tree tree(String sha) {
		log.debug("GET tree {}", sha);
		return gitClient.tree(sha).orElseThrow();
	}

	@Cacheable(cacheNames = "commits", keyGenerator = "keyGen")
	private Commit commit(String sha) {
		log.debug("GET commit {}", sha);
		return gitClient.commit(sha).orElseThrow();
	}

	@Cacheable(cacheNames = "refs", keyGenerator = "keyGen")
	private Optional<Reference> ref(String ref) {
		log.debug("GET ref {}", ref);
		return gitClient.ref(ref);
	}

	private Reference branch(String branchName) {
		log.info("Obtaining branch {}", branchName);
		Reference main = ref("main").orElseThrow();
		branchRequest.setSha(main.getObject().getSha());
		branchRequest.setRef("refs/heads/" + branchName);
		return ref(branchName).orElseGet(() -> gitClient.createBranch(branchRequest));
	}

	public List<Branch> branches() {
		return gitClient.branches()
				.stream().filter(b -> !b.getName().contains("main"))
				.collect(Collectors.toList());
	}

	@SneakyThrows
	public Commit save(String branchName, String path, T payload, String message) {
		log.info("POST '{}' into '{}' for task: '{}'", path, branchName, message);
		Reference branch = branch(branchName);
		blobRequest.setContent(objectMapper.writeValueAsString(payload));
		Commit lastCommit = commit(branch.getObject().getSha());
		treeRequest.setBaseTree(tree(lastCommit.getCommitDetails().getTree().getSha()).getSha());
		treeRequest.setTree(List.of(new TreeDetail(gitClient.createBlob(blobRequest).getSha(),
				path,
				BLOB,
				BLOB_MODE)));
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
		log.info("GET content of '{}' and cast to {}", path, c.getSimpleName());
		return decode(commit(sha)
				.getFiles()
				.stream().filter(f -> path.contains(f.getFilename()))
				.map(f -> blob(f.getSha()))
				.findFirst().orElseThrow()
				.getBase64content(), c);
	}

	public void merge(String branchName, String message) {
		log.info("Merged branch [{}] >>> {}", branchName, message);
		String head = ref(branchName).orElseThrow()
				.getObject()
				.getSha();
		mergeRequest.setBase("main");
		mergeRequest.setHead(head);
		mergeRequest.setCommitMessage(message);
		gitClient.merge(mergeRequest);
		gitClient.deleteBranch(branchName);
	}

	public List<T> history(String path, Predicate<Commit> filetrCriteria, Class<T> c) {
		log.info("GET commits '{}'", path);
		return gitClient.commits()
				.parallelStream()
				.filter(filetrCriteria)
				.filter(c1 -> c1.getCommitDetails().getMessage().contains("Merge"))
				.map(c2 -> commit(c2.getSha()))
				.filter(c3 -> c3.getFiles() != null && !c3.getFiles().isEmpty())
				.map(c4 -> blob(c4.getFiles()
						.stream()
						.filter(f -> path.contains(f.getFilename()))
						.findFirst().orElseThrow()
						.getSha()).getBase64content())
				.map(coded -> decode(coded, c))
				.collect(Collectors.toList());
	}

	@SneakyThrows
	private T decode(String s, Class<T> c) {
		return objectMapper.readValue(Base64.getDecoder().decode(
				s.replace("\n", "")
						.replace("\r", "")), c);
	}
}
