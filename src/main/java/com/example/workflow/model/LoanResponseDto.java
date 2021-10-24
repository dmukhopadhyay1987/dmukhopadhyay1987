package com.example.workflow.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
public class LoanResponseDto {

	String loan;
	String customerId;
	Double remainingPrincipal;
	Integer remainingTerm;
	String productType;
}
