package com.chatbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "whatsapp")
@Data
public class WhatsAppConfig {
    private String phoneNumberId;
    private String accessToken;
    private String verifyToken;
    private String apiUrl;
}
