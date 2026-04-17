package com.avijeet.rebase.service.auth;

import com.avijeet.rebase.entities.Session;
import com.avijeet.rebase.entities.User;
import com.avijeet.rebase.exceptions.InvalidTokenException;
import com.avijeet.rebase.repository.auth.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {
    private final SessionRepository sessionRepository;
    private final TokenService tokenService;

    @Value("${security.session.expiry-hours:168}")
    private long sessionExpiryHours;

    @Transactional
    public Session createSession(User user, String userAgent, String ipAddress) {
        invalidateActiveSessions(user);

        Session session = Session.builder()
                .user(user)
                .refreshToken(tokenService.generateRefreshToken())
                .userAgent(userAgent)
                .ipAddress(ipAddress)
                .expiresAt(LocalDateTime.now().plusHours(sessionExpiryHours))
                .active(true)
                .build();

        Session savedSession = sessionRepository.save(session);
        log.info("Created session sessionId={} userId={} ip={}", savedSession.getId(), user.getId(), ipAddress);
        return savedSession;
    }

    @Transactional(readOnly = true)
    public Session getActiveSession(Long sessionId) {
        Session session = sessionRepository.findByIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new InvalidTokenException("Session does not exist or is inactive"));

        if (session.isExpired()) {
            log.warn("Session expired sessionId={}", sessionId);
            throw new InvalidTokenException("Session has expired");
        }

        return session;
    }

    @Transactional(readOnly = true)
    public Session getActiveSessionByRefreshToken(String refreshToken) {
        Session session = sessionRepository.findByRefreshTokenAndActiveTrue(refreshToken)
                .orElseThrow(() -> new InvalidTokenException("Refresh token is invalid"));

        if (session.isExpired()) {
            log.warn("Session expired during refresh sessionId={}", session.getId());
            throw new InvalidTokenException("Session has expired");
        }

        return session;
    }

    @Transactional
    public void invalidateActiveSessions(User user) {
        List<Session> activeSessions = sessionRepository.findByUserAndActiveTrue(user);
        if (activeSessions.isEmpty()) {
            return;
        }

        activeSessions.forEach(session -> session.setActive(false));
        sessionRepository.saveAll(activeSessions);
        log.info("Invalidated {} active session(s) for userId={}", activeSessions.size(), user.getId());
    }

    @Transactional
    public void revokeSession(Long sessionId) {
        sessionRepository.findByIdAndActiveTrue(sessionId).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
            log.info("Revoked session sessionId={} userId={}", sessionId, session.getUser().getId());
        });
    }
}

