package com.avijeet.rebase.dto.auth;

import com.avijeet.rebase.dto.profile.UserProfileResponse;

import java.time.LocalDateTime;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        Long sessionId,
        LocalDateTime sessionExpiresAt,
        UserProfileResponse user
) {
}

