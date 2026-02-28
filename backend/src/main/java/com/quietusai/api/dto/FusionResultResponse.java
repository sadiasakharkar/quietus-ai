package com.quietusai.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FusionResultResponse(
        UUID sessionId,
        UUID chunkId,
        String finalRisk,
        BigDecimal finalScore,
        BigDecimal confidence,
        List<String> modalitiesUsed,
        OffsetDateTime computedAt
) {
}
