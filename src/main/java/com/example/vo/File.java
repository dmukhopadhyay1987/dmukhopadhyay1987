package com.example.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class File {

	String sha;
	String filename;
	String status;
	int additions;
	int deletions;
	int changes;
}
