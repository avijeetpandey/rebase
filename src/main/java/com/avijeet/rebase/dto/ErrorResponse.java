package com.avijeet.rebase.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
        String message,
        String details,
        LocalDateTime timestamp
) {
}

