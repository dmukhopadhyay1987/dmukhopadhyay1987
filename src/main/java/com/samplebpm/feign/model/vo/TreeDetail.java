package com.samplebpm.feign.model.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreeDetail {

	@NonNull
	private String sha;
	@NonNull
	private String path;
	@NonNull
	private String type;
	@NonNull
	private String mode;
}
