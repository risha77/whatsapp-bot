package com.chatbot.config;

import org.springframework.context.annotation.Configuration;

/**
 * MongoDB is auto-configured via spring.data.mongodb.uri.
 * Add custom MongoClient beans here if needed in Phase 2
 * (e.g. read-preference, connection pool tuning).
 */
@Configuration
public class MongoConfig {
    // Auto-configured — no custom beans required for MVP
}
