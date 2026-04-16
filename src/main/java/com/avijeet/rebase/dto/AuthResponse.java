package com.avijeet.rebase.dto;

import java.time.LocalDateTime;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long sessionId,
        LocalDateTime sessionExpiresAt,
        UserProfileResponse user
) {
}

