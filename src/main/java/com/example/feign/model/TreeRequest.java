package com.example.feign.model;

import com.example.vo.TreeDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreeRequest {

	@JsonProperty("base_tree")
	String baseTree;
	List<TreeDetail> tree;
}
