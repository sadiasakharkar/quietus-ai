package com.quietusai.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubmitAnalysisRequest(
        @NotBlank @Size(min = 5, max = 5000) String text,
        @NotBlank @Size(max = 32) String source
) {
}
