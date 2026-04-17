package com.avijeet.rebase.dto.profile;

import java.util.List;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        String bio,
        String avatarUrl,
        List<String> techStack
) { }

