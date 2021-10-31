package com.example.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Value
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReportInfo {

	String startDateTime;
	List<LoanReportInfo> loanReportInfos;
	String endDateTime;
}
