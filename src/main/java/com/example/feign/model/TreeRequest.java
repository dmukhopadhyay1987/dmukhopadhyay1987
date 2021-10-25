package com.example.feign.model;

import com.example.vo.Tree;
import com.example.vo.TreeDetail;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreeRequest {

	String baseTree;
	List<TreeDetail> tree;
}
