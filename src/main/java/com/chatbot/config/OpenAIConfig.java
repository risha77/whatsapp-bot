package com.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "openai")
@Data
public class OpenAIConfig {
    private String apiKey;
    private String model;
    private Integer maxTokens;
    private Double temperature;
}
