package com.quietusai.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record StartSessionResponse(
        UUID sessionId,
        String status,
        OffsetDateTime startedAt
) {
}
