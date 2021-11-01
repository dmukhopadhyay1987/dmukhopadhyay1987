package com.example.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.dozer.Mapping;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProposalRequestDto {
	@Mapping("loan")
	String loanNum;
	@Mapping("customerId")
	String custId;
	@Mapping("remainingPrincipal")
	Double remPrincipalAmt;
	@Mapping("remainingTerm")
	Integer remTerm;
	@Mapping("productType")
	String product;
}
