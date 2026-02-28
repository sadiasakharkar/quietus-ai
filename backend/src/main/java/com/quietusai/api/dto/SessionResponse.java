package com.quietusai.api.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID sessionId,
        UUID userId,
        String status,
        OffsetDateTime startedAt,
        OffsetDateTime endedAt
) {
}
