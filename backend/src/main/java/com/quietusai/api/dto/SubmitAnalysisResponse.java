package com.quietusai.api.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record SubmitAnalysisResponse(
        UUID analysisId,
        String label,
        BigDecimal confidence,
        String modelVersion,
        OffsetDateTime analyzedAt
) {
}
