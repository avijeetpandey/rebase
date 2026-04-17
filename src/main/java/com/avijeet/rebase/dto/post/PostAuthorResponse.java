package com.avijeet.rebase.dto.post;

public record PostAuthorResponse(
        Long id,
        String username,
        String avatarUrl
) {}