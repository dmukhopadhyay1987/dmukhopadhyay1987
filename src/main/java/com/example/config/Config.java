package com.example.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class Config {

	public static final String PROCESS_BUSINESS_KEY_DELIMITER = "##_#";
	private static final String LOAN_NUMBER_VARIABLE_KEY = "loanNumber";
	private static final String LOAN_NUMBERS_VARIABLE_KEY = "loanNumbers";
	private static final String PROPOSAL_REQUEST_VARIABLE_KEY = "proposalRequestDto";
	private static final String PROPOSAL_RESPONSE_VARIABLE_KEY = "proposalResponseDto";

	@Bean("processBusinessKeyDelimiter") String processBusinessKeyDelimiter() {
		return PROCESS_BUSINESS_KEY_DELIMITER;
	}

	@Bean("loanVariableKey") String loanVariableKey() {
		return LOAN_NUMBER_VARIABLE_KEY;
	}

	@Bean("loansVariableKey") String loansVariableKey() {
		return LOAN_NUMBERS_VARIABLE_KEY;
	}

	@Bean("proposalRequestVariableKey") String proposalRequestVariableKey() {
		return PROPOSAL_REQUEST_VARIABLE_KEY;
	}

	@Bean("proposalResponseVariableKey") String proposalResponseVariableKey() {
		return PROPOSAL_RESPONSE_VARIABLE_KEY;
	}

	@Bean
	@Primary
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		SimpleModule simpleModule = new SimpleModule();
		simpleModule.addDeserializer(String.class, new StringDeserializer());
		mapper.registerModule(simpleModule);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
		mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
		mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
		mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
		mapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
		mapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
		return mapper;
	}

}
