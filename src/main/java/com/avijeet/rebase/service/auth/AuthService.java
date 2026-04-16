package com.avijeet.rebase.service.auth;

import com.avijeet.rebase.dto.AuthResponse;
import com.avijeet.rebase.dto.LoginRequest;
import com.avijeet.rebase.dto.RegisterRequest;
import com.avijeet.rebase.dto.UserProfileResponse;
import com.avijeet.rebase.entities.Session;
import com.avijeet.rebase.entities.User;
import com.avijeet.rebase.exceptions.AuthenticationFailedException;
import com.avijeet.rebase.exceptions.UserAlreadyExisitsException;
import com.avijeet.rebase.exceptions.UserNotFoundException;
import com.avijeet.rebase.repository.UserRepository;
import com.avijeet.rebase.utils.mapper.AuthObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final JwtService jwtService;
    private final AuthObjectMapper authObjectMapper;

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UserAlreadyExisitsException("Username already in use");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExisitsException("Email already in use");
        }

        User savedUser = userRepository.save(User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build());

        return authObjectMapper.toUserProfile(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }

        Session session = sessionService.createSession(user, userAgent, ipAddress);
        String accessToken = jwtService.generateAccessToken(user.getUsername(), session.getId().toString());
        return authObjectMapper.toAuthResponse(user, session, accessToken);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(String username, UUID sessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Session session = sessionService.getActiveSession(sessionId);
        if (!session.getUser().getId().equals(user.getId())) {
            throw new AuthenticationFailedException("Session does not belong to authenticated user");
        }

        return authObjectMapper.toUserProfile(user);
    }

    @Transactional
    public void logout(UUID sessionId) {
        sessionService.revokeSession(sessionId);
    }
}

