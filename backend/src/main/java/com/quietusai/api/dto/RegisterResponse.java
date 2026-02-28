package com.quietusai.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        String email,
        List<String> roles,
        OffsetDateTime createdAt
) {
}
