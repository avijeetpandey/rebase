package com.avijeet.rebase.dto.profile;

import jakarta.validation.constraints.Size;

import java.util.List;

public record UpdateProfileRequest(
        @Size(max = 500, message = "Bio cannot exceed 500 characters")
        String bio,

        @Size(max = 10, message = "Cannot select more than 10 technologies")
        List<String> techStack
) {}