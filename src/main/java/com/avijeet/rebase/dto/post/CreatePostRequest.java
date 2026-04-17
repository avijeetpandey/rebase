package com.avijeet.rebase.dto.post;

import jakarta.validation.constraints.Size;

public record CreatePostRequest(
        @Size(min = 1, max = 2000, message = "Content must be between 1 and 2000 characters")
        String content,
        @Size(max = 5000, message = "Code snippet is too large")
        String codeSnippet,
        String codeLanguage
) { }
