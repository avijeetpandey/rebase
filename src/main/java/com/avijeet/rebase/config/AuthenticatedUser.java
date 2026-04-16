package com.avijeet.rebase.config;

public record AuthenticatedUser(
        String username,
        Long userId,
        Long sessionId
) {
}

