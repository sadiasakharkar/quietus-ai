package com.quietusai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
        Jwt jwt,
        Ai ai
) {
    public record Jwt(String secret, long expirationSeconds) {
    }

    public record Ai(String baseUrl, String serviceToken) {
    }
}
