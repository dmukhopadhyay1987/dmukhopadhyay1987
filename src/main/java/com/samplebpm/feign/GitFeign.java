package com.samplebpm.feign;

import com.samplebpm.feign.config.GitFeignConfig;
import com.samplebpm.feign.model.BlobRequest;
import com.samplebpm.feign.model.BranchRequest;
import com.samplebpm.feign.model.BranchUpdateRequest;
import com.samplebpm.feign.model.CommitRequest;
import com.samplebpm.feign.model.MergeRequest;
import com.samplebpm.feign.model.TreeRequest;
import com.samplebpm.feign.model.vo.Blob;
import com.samplebpm.feign.model.vo.Branch;
import com.samplebpm.feign.model.vo.Commit;
import com.samplebpm.feign.model.vo.Reference;
import com.samplebpm.feign.model.vo.Tree;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "gitHubClient",
		url = "${db.iwonosql.connection.url}",
		configuration = { GitFeignConfig.class },
		decode404 = true)
public interface GitFeign {

	@GetMapping("/branches")
	List<Branch> branches();

	@GetMapping("/commits/{sha}")
	Optional<Commit> commit(@PathVariable("sha") String sha);

	@GetMapping("/git/blobs/{sha}")
	Optional<Blob> blob(@PathVariable("sha") String sha);

	@GetMapping("/git/refs/heads/{ref}")
	Optional<Reference> ref(@PathVariable("ref") String refName);

	@GetMapping("/commits")
	List<Commit> commits();

	@PostMapping(value = "/git/blobs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Blob createBlob(@RequestBody BlobRequest blobReq);

	@PostMapping(value = "/git/trees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Tree createTree(@RequestBody TreeRequest treeReq);

	@PostMapping(value = "/git/refs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Reference createBranch(@RequestBody BranchRequest branchReq);

	@PatchMapping(value = "/git/refs/heads/{branch}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	void updateBranch(@PathVariable("branch") String branch, @RequestBody BranchUpdateRequest branchUpdateReq);

	@PostMapping(value = "/git/commits", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Commit createCommit(@RequestBody CommitRequest commitReq);

	@PostMapping(value = "/merges", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	void merge(@RequestBody MergeRequest mergeReq);

	@DeleteMapping("/git/refs/heads/{branch}")
	void deleteBranch(@PathVariable("branch") String branch);
}
