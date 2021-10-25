package com.example.feign;

import com.example.feign.config.GitFeignConfig;
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
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "gitHubClient", url = "https://api.github.com/repos/dmukhopadhyay1987/iwonosqldb", configuration = { GitFeignConfig.class}, decode404 = true)
public interface GitFeign {

	// GET Mappings
	@GetMapping("/branches")
	List<Branch> branches();

	@GetMapping("/commits/{sha}")
	Optional<Commit> commit(@PathVariable(value = "sha") String sha);

	@GetMapping("/git/trees/{sha}")
	Optional<Tree> tree(@PathVariable(value = "sha") String sha);

	@GetMapping("/git/blobs/{sha}")
	Optional<Blob> blob(@PathVariable(value = "sha") String sha);

	@GetMapping("/git/refs/heads/{ref}")
	Optional<Reference> ref(@PathVariable(value = "ref") String refName);

	// POST Mappings
	@PostMapping(value = "/git/blobs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Blob createBlob(@RequestBody BlobRequest blobReq);

	@PostMapping(value = "/git/trees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Tree createTree(@RequestBody TreeRequest treeReq);

	@PostMapping(value = "/git/refs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Reference createBranch(@RequestBody BranchRequest branchReq);

	@PostMapping(value = "/git/refs/heads/{branch}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Branch updateBranch(@PathVariable(value = "branch") String branch, @RequestBody BranchUpdateRequest branchUpdateReq);

	@PostMapping(value = "/git/commits", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Commit createCommit(@RequestBody CommitRequest commitReq);

	@PostMapping(value = "/merges", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Commit merge(@RequestBody MergeRequest mergeReq);
}
