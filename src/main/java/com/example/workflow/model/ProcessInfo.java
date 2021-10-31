package com.example.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

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
	LoanResponseDto loanDetails;
	ProposalResponseDto proposalDetails;
	String endDateTime;
	String status;
	List<ProcessInfo> history;
}
