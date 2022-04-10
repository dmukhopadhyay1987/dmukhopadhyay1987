package com.samplebpm.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanReportInfo {

	String loanNumber;
	List<LoanModificationStageInfo> stages;
	LoanStatus status;
}
