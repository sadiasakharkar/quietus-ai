package com.quietusai.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpsertTextPredictionRequest(
        @NotBlank String transcript,
        @NotBlank String riskLevel,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal distressProbability,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidence
) {
}
