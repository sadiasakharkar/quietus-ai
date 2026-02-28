package com.quietusai.api.dto;

import java.util.List;
import java.util.UUID;

public record UserInfoResponse(
        UUID userId,
        String email,
        List<String> roles
) {
}
