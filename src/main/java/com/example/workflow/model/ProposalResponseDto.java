package com.example.workflow.model;

import lombok.*;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
public class ProposalResponseDto {

	String loan;
	String customerId;
	Integer proposedTerm;
	Double proposedInterestRate;
}
