package com.quietusai.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpsertVisionPredictionRequest(
        @NotBlank String emotion,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidence,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal distressScore,
        @Min(0) Integer framesProcessed
) {
}
