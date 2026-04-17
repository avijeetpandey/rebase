package com.avijeet.rebase.dto.post;

public record CommentResponse(
        Long id,
        String content,
        String createdAt,
        CommentAuthorResponse author
) {}

