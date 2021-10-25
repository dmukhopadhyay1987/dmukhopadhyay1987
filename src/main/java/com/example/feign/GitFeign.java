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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;
import java.util.Optional;

@FeignClient(name = "gitHubClient", url = "https://api.github.com", configuration = { GitFeignConfig.class}, decode404 = true)
public interface GitFeign {

	@RequestMapping(method = RequestMethod.GET, value = "/repos/dmukhopadhyay1987/iwonosqldb/branches")
	List<Branch> branches();

	@RequestMapping(method = RequestMethod.GET, value = "/repos/dmukhopadhyay1987/iwonosqldb/commits/{sha}")
	Optional<Commit> commit(@PathVariable(value = "sha") String sha);

	@RequestMapping(method = RequestMethod.GET, value = "/repos/dmukhopadhyay1987/iwonosqldb/git/trees/{sha}")
	Optional<Tree> tree(@PathVariable(value = "sha") String sha);

	@RequestMapping(method = RequestMethod.GET, value = "/repos/dmukhopadhyay1987/iwonosqldb/git/blobs/{sha}")
	Optional<Blob> blob(@PathVariable(value = "sha") String sha);

	@RequestMapping(method = RequestMethod.GET, value = "/repos/dmukhopadhyay1987/iwonosqldb/git/refs/heads/{ref}")
	Optional<Reference> ref(@PathVariable(value = "ref") String refName);

	@RequestMapping(method = RequestMethod.POST, value = "/repos/dmukhopadhyay1987/iwonosqldb/git/blobs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Blob createBlob(@RequestBody BlobRequest blobReq);

	@RequestMapping(method = RequestMethod.POST, value = "/repos/dmukhopadhyay1987/iwonosqldb/git/trees", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Tree createTree(@RequestBody TreeRequest treeReq);

	@RequestMapping(method = RequestMethod.POST, value = "/repos/dmukhopadhyay1987/iwonosqldb/git/refs", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Reference createBranch(@RequestBody BranchRequest branchReq);

	@RequestMapping(method = RequestMethod.POST, value = "/repos/dmukhopadhyay1987/iwonosqldb/git/refs/heads/{branch}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Branch updateBranch(@PathVariable(value = "branch") String branch, @RequestBody BranchUpdateRequest branchUpdateReq);

	@RequestMapping(method = RequestMethod.POST, value = "/repos/dmukhopadhyay1987/iwonosqldb/git/commits", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Commit createCommit(@RequestBody CommitRequest commitReq);

	@RequestMapping(method = RequestMethod.POST, value = "/repos/dmukhopadhyay1987/iwonosqldb/merges", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	Commit merge(@RequestBody MergeRequest mergeReq);
}
