package com.samplebpm.workflow.services;

import com.iwonosql.feign.GitFeign;
import com.iwonosql.feign.model.BlobRequest;
import com.iwonosql.feign.model.BranchRequest;
import com.iwonosql.feign.model.BranchUpdateRequest;
import com.iwonosql.feign.model.CommitRequest;
import com.iwonosql.feign.model.MergeRequest;
import com.iwonosql.feign.model.TreeRequest;
import com.iwonosql.feign.model.vo.Blob;
import com.iwonosql.feign.model.vo.Branch;
import com.iwonosql.feign.model.vo.Commit;
import com.iwonosql.feign.model.vo.File;
import com.iwonosql.feign.model.vo.Reference;
import com.iwonosql.feign.model.vo.Tree;
import com.iwonosql.feign.model.vo.TreeDetail;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
@Slf4j
@EnableCaching
public class PersistenceService<T> {

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
		log.debug("Obtaining branch {}", branchName);
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
	public void save(String branchName, String path, T payload, String message) {
		log.info("POST '{}' into '{}' for task: '{}'", path, branchName, message);
		Reference branch = branch(branchName);
		blobRequest.setContent(objectMapper.writeValueAsString(payload));
		Commit lastCommit = commit(branch.getObject().getSha());
		treeRequest.setBaseTree(lastCommit.getSha());
		treeRequest.setTree(List.of(new TreeDetail(gitClient.createBlob(blobRequest).getSha(),
				path,
				BLOB,
				BLOB_MODE)));
		Tree tree = gitClient.createTree(treeRequest);
		commitRequest.setTree(tree.getSha());
		commitRequest.setMessage(message);
		commitRequest.setParents(List.of(lastCommit.getSha()));
		branchUpdateRequest.setSha(gitClient.createCommit(commitRequest).getSha());
		gitClient.updateBranch(branch.getRef().replace("refs/heads/", ""), branchUpdateRequest);
	}

	@SneakyThrows
	public T get(String path, String branchName, Class<T> c) {
		log.debug("GET content of '{}' and cast to {}", path, c.getSimpleName());
		return decode(commit(branch(branchName).getObject().getSha())
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

	public List<T> mergeHistory(String path, Predicate<Commit> filetrCriteria, Class<T> c) {
		log.info("GET merge commits '{}'", path);
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

	public Map<String, T> history(String path, Predicate<Commit> filetrCriteria, Class<T> c) {
		log.info("GET commits '{}'", path);
		Map<String, T> historyMap = new TreeMap<>();
		gitClient.commits()
				.parallelStream()
				.filter(filetrCriteria)
				.filter(c1 -> !c1.getCommitDetails().getMessage().contains("Merge"))
				.map(c2 -> commit(c2.getSha()))
				.filter(c3 -> c3.getFiles() != null && !c3.getFiles().isEmpty())
				.toList()
				.forEach(c4 -> {
					File file = c4.getFiles()
							.stream()
							.filter(f -> path.contains(f.getFilename()))
							.findFirst().orElse(null);
					if (file != null) {
						String date = c4.getCommitDetails().getCommitter().getDate();
						historyMap.put(date,
								decode(blob(file.getSha()).getBase64content(), c));
					}
				});
		return historyMap;
	}

	@SneakyThrows
	private T decode(String s, Class<T> c) {
		return objectMapper.readValue(Base64.getDecoder().decode(
				s.replace("\n", "")
						.replace("\r", "")), c);
	}
}
