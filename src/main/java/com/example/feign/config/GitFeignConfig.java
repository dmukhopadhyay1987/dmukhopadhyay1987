package com.example.feign.config;

import feign.RequestInterceptor;
import feign.okhttp.OkHttpClient;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GitFeignConfig {

	@Bean
	public OkHttpClient client() {
		return new OkHttpClient();
	}

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			requestTemplate.header("accept", " application/vnd.github.v3.full+json");
			requestTemplate.header("Authorization", "Token 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");
		};
	}

	@Bean
	public KeyGenerator keyGen() {
		return (target, method, params) -> params[0];
	}
}
