package com.samplebpm.feign.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Commit {

	@NonNull
	private String sha;
	@JsonProperty("commit")
	private CommitDetails commitDetails;
	private List<Commit> parents;
	private List<File> files;
}
