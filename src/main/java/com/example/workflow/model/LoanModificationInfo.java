package com.example.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;

import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanModificationInfo {

	@NotNull
	String loanNumber;
	@NotNull
	String startDateTime;
	LoanResponseDto loanDetails;
	ProposalResponseDto proposalDetails;
	String endDateTime;
	LoanStatus status;
	List<LoanModificationInfo> history;
}
