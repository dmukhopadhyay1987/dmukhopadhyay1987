package com.iwonosql.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

@Component
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchRequest {

	@NotNull
	String sha;
	@NotNull
	String ref;
}
