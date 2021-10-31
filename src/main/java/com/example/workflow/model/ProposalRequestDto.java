package com.example.workflow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Value;
import org.dozer.Mapping;
import org.springframework.stereotype.Component;

@Value
@Builder
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
