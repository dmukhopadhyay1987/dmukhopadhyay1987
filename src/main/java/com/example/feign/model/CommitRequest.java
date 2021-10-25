package com.example.feign.model;

import com.example.vo.Commit;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommitRequest {

	String tree;
	String message;
	List<String> parents;
}
