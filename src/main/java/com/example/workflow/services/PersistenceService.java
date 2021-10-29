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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
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

	@SneakyThrows
	public Commit save(String branchName, String path, T payload, String message) {
		log.info("POST '{}' into '{}' for task: '{}'", path, branchName, message);
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
		log.info("GET content of '{}' and cast to {}", path, c.getSimpleName());
		return objectMapper.readValue(Base64.getDecoder().decode(tree(commit(sha).getCommitDetails().getTree().getSha())
				.getTreeDetail().stream()
				.filter(t -> path.contains(t.getPath()))
				.map(td -> tree(td.getSha()))
				.map(bt -> blob(bt.getTreeDetail().stream().findFirst().orElseThrow().getSha()))
				.findFirst().orElseThrow()
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

	public CompletableFuture<List<Commit>> history(String path) {
		log.info("GET commits '{}'", path);
		return CompletableFuture.supplyAsync(() -> chain(path,
				commit(ref("main").orElseThrow()
						.getObject()
						.getSha()), null));
	}

	private List<Commit> chain(String path, Commit head, final List<Commit> list) {
		List<Commit> localList = new CopyOnWriteArrayList<>(
				Objects.requireNonNullElseGet(
						list,
						() -> !head.getCommitDetails().getMessage().contains("Merge") ? List.of(head) : List.of()));
		List<Commit> parents = commit(head.getSha()).getParents();
		Commit parent = parents.stream()
				.map(p -> commit(p.getSha()))
				.max(Comparator.comparing(
						a -> LocalDateTime.parse(
								a.getCommitDetails().getCommitter().getDate(),
								DateTimeFormatter.ISO_DATE_TIME)))
				.orElse(null);
		if (parent != null && tree(
				parent.getCommitDetails().getTree().getSha())
				.getTreeDetail()
				.stream().anyMatch(
						t -> t.getType().equals("tree")
								&& path.contains(t.getPath()))) {
			if (!parent.getCommitDetails()
					.getMessage()
					.contains("Merge")) localList.add(parent);
			return chain(path, commit(parent.getSha()), localList);
		}
		return localList;
	}
}
