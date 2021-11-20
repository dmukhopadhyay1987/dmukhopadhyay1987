package com.example.feign.config;

import feign.RequestInterceptor;
import feign.okhttp.OkHttpClient;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "db.iwonosql.connection.auth")
@NoArgsConstructor
@Setter
public class GitFeignConfig {

	private String key;

	@Bean
	public OkHttpClient client() {
		return new OkHttpClient();
	}

	@Bean
	public RequestInterceptor requestInterceptor() {
		return requestTemplate -> {
			requestTemplate.header("accept", " application/vnd.github.v3.full+json");
			requestTemplate.header("Authorization", "Token " + key);
		};
	}

	@Bean
	public KeyGenerator keyGen() {
		return (target, method, params) -> params[0];
	}
}
