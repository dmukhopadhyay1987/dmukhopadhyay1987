package com.iwonosql.feign.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class BranchUpdateRequest {

	String sha;
}
