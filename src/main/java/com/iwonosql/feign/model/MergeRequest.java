package com.iwonosql.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MergeRequest {

	@NotNull
	String base;
	@NotNull
	String head;
	@JsonProperty("commit_message")
	String commitMessage;
}
