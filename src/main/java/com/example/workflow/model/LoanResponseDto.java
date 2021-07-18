package com.example.workflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Getter
@Setter
@NoArgsConstructor
public class LoanResponseDto {

	String loan;
	String customerId;
	Double remainingPrincipal;
	Integer remainingTerm;
	Date loanStartDate;
	Date loanEndDate;
	String productType;
}
