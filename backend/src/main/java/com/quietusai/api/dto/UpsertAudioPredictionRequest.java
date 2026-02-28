package com.quietusai.api.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpsertAudioPredictionRequest(
        @NotBlank String emotion,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal stressScore,
        @DecimalMin("0.0") @DecimalMax("1.0") BigDecimal confidence
) {
}
