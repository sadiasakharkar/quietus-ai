package com.quietusai.api.dto;

import java.util.UUID;

public record PredictionAckResponse(
        UUID sessionId,
        UUID chunkId,
        String modality,
        String status
) {
}
