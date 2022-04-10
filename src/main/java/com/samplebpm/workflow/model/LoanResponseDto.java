package com.samplebpm.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoanResponseDto {

	String loan;
	String customerId;
	Double remainingPrincipal;
	Integer remainingTerm;
	String productType;
}
