package com.quietusai.infrastructure.client;

public record AiPredictRequest(
        String text,
        String requestId
) {
}
