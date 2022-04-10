package com.samplebpm.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportInfo {

	String startDateTime;
	String lastRunTime;
	List<LoanReportInfo> loanReportInfos;
	String endDateTime;
}
