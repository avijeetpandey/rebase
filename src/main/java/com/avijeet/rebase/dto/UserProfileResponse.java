package com.avijeet.rebase.dto;

public record UserProfileResponse(
        Long id,
        String username,
        String email
) {
}

