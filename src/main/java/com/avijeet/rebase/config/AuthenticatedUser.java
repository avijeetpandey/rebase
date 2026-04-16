package com.avijeet.rebase.config;

import java.util.UUID;

public record AuthenticatedUser(
        String username,
        Long userId,
        UUID sessionId
) {
}

