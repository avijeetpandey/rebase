package com.avijeet.rebase.dto.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Error details payload")
public record ErrorResponse(
        @Schema(example = "Validation failed")
        String message,
        @Schema(example = "username must not be blank")
        String details,
        @Schema(example = "2026-04-17T23:21:05.460")
        LocalDateTime timestamp
) {
}

