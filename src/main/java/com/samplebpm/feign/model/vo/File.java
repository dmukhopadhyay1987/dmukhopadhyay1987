package com.samplebpm.feign.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class File {

	@NonNull
	String sha;
	String filename;
	String status;
	int additions;
	int deletions;
	int changes;
	String patch;
}
