package com.quietusai.infrastructure.client;

import java.math.BigDecimal;

public record AiPredictResponse(
        String label,
        BigDecimal confidence,
        String modelVersion,
        Integer processingMs
) {
}
