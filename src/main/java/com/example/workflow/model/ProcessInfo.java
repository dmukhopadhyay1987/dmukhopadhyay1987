package com.example.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
@Component
@Setter
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessInfo {

	@NotNull
	String loanNumber;
	@NotNull
	String startDateTime;
	@NotNull
	String processingDate;
	LoanResponseDto loanDetails;
	ProposalResponseDto proposalDetails;
	String endDateTime;
}
