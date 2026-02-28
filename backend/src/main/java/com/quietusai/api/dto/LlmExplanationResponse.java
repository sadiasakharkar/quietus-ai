package com.quietusai.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record LlmExplanationResponse(
        UUID sessionId,
        UUID chunkId,
        String summary,
        List<String> keySignals,
        String recommendedActionLevel,
        BigDecimal explanationConfidence,
        OffsetDateTime createdAt
) {
}
