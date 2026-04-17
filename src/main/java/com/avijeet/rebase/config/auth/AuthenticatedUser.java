package com.avijeet.rebase.config.auth;

public record AuthenticatedUser(
        String username,
        Long userId,
        Long sessionId
) {
}

