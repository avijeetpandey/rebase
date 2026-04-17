package com.avijeet.rebase.dto.post;

public record PostResponse(
        Long id,
        String content,
        String imageUrl,
        String codeSnippet,
        String codeLanguage,
        String createdAt,
        int lgtmCount,
        PostAuthorResponse author
) {}