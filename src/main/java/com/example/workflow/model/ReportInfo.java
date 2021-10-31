package com.example.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
@JsonDeserialize(builder = ReportInfo.ReportInfoBuilder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportInfo {

	String startDateTime;
	List<LoanReportInfo> loanReportInfos;
	String endDateTime;
}
