package com.avijeet.rebase.service.auth;

import com.avijeet.rebase.dto.AuthResponse;
import com.avijeet.rebase.dto.LoginRequest;
import com.avijeet.rebase.dto.RefreshTokenRequest;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private final JwtService jwtService;
    private final AuthObjectMapper authObjectMapper;

    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            log.warn("Register failed: username already exists username={}", request.username());
            throw new UserAlreadyExisitsException("Username already in use");
        }

        if (userRepository.existsByEmail(request.email())) {
            log.warn("Register failed: email already exists username={}", request.username());
            throw new UserAlreadyExisitsException("Email already in use");
        }

        User savedUser = userRepository.save(User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .build());

        log.info("Registered user userId={} username={}", savedUser.getId(), savedUser.getUsername());

        return authObjectMapper.toUserProfile(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String userAgent, String ipAddress) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Login failed: invalid password username={} ip={}", request.username(), ipAddress);
            throw new AuthenticationFailedException("Invalid username or password");
        }

        Session session = sessionService.createSession(user, userAgent, ipAddress);
        String accessToken = jwtService.generateAccessToken(user.getUsername(), session.getId().toString());
        log.info("Login successful username={} sessionId={}", user.getUsername(), session.getId());
        return authObjectMapper.toAuthResponse(user, session, accessToken);
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getCurrentUser(String username, Long sessionId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Session session = sessionService.getActiveSession(sessionId);
        if (!session.getUser().getId().equals(user.getId())) {
            log.warn("Profile access denied: session does not belong to user username={} sessionId={}", username, sessionId);
            throw new AuthenticationFailedException("Session does not belong to authenticated user");
        }

        return authObjectMapper.toUserProfile(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refreshAccessToken(RefreshTokenRequest request) {
        Session session = sessionService.getActiveSessionByRefreshToken(request.refreshToken());
        User user = session.getUser();

        String accessToken = jwtService.generateAccessToken(user.getUsername(), session.getId().toString());
        log.info("Access token refreshed username={} sessionId={}", user.getUsername(), session.getId());
        return authObjectMapper.toAuthResponse(user, session, accessToken);
    }

    @Transactional
    public void logout(Long sessionId) {
        sessionService.revokeSession(sessionId);
        log.info("Logout processed for sessionId={}", sessionId);
    }
}

