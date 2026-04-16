package com.avijeet.rebase.utils.mapper;

import com.avijeet.rebase.dto.AuthResponse;
import com.avijeet.rebase.dto.UserProfileResponse;
import com.avijeet.rebase.entities.Session;
import com.avijeet.rebase.entities.User;
import org.springframework.stereotype.Component;

@Component
public class AuthObjectMapper {

    public UserProfileResponse toUserProfile(User user) {
        return new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
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

