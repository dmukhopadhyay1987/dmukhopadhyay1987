package com.example.config;

import lombok.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "app.sample.config.keys")
@NoArgsConstructor
@Getter
@Setter
public class ConfigProperties {

    String loanNumber;
    String loanNumbers;
    String proposalRequest;
    String proposalResponse;
    String report;
}
