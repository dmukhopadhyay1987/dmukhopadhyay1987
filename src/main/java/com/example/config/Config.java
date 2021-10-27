package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

	public static final String PROCESS_BUSINESS_KEY_DELIMITER = "##_#";
	private static final String LOAN_NUMBER_VARIABLE_KEY = "loanNumber";
	private static final String PROPOSAL_REQUEST_VARIABLE_KEY = "proposalRequestDto";
	private static final String PROPOSAL_RESPONSE_VARIABLE_KEY = "proposalResponseDto";


	@Bean String processBusinessKeyDelimiter() {
		return PROCESS_BUSINESS_KEY_DELIMITER;
	}

	@Bean String loanVariableKey() {
		return LOAN_NUMBER_VARIABLE_KEY;
	}

	@Bean String proposalRequestVariableKey() {
		return PROPOSAL_REQUEST_VARIABLE_KEY;
	}

	@Bean String proposalResponseVariableKey() {
		return PROPOSAL_RESPONSE_VARIABLE_KEY;
	}

}
