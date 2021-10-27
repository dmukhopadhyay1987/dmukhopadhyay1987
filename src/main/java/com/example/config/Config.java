package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {

	public static final String PROCESS_BUSINESS_KEY_DELIMITER = "##_#";

	@Bean String processBusinessKeyDelimiter() {
		return PROCESS_BUSINESS_KEY_DELIMITER;
	}

}
