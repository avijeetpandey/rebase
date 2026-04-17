package com.avijeet.rebase.utils.mapper;

import com.avijeet.rebase.dto.auth.AuthResponse;
import com.avijeet.rebase.dto.profile.UserProfileResponse;
import com.avijeet.rebase.entities.Session;
import com.avijeet.rebase.entities.User;
import com.avijeet.rebase.entities.UserProfile;
import org.springframework.stereotype.Component;

@Component
public class AuthObjectMapper {

    public UserProfileResponse toUserProfile(User user) {
        UserProfile profile = user.getProfile();
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                profile != null ? profile.getBio() : null,
                profile != null ? profile.getAvatarUrl() : null,
                profile != null ? profile.getTechStack() : null
        );
    }

    public AuthResponse toAuthResponse(User user, Session session, String accessToken) {
        return new AuthResponse(
                accessToken,
                session.getRefreshToken(),
                session.getId(),
                session.getExpiresAt(),
                toUserProfile(user)
        );
    }
}
