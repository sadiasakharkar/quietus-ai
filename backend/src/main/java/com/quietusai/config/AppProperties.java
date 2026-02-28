package com.quietusai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Llm llm
) {
    public record Jwt(String secret, long expirationSeconds) {
    }

    public record Llm(
            String provider,
            String openaiBaseUrl,
            String openaiApiKey,
            String openaiModel,
            String geminiBaseUrl,
            String geminiApiKey,
            String geminiModel
    ) {
    }
}
