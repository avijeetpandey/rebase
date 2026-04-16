package com.avijeet.rebase.service.auth;

import com.avijeet.rebase.entities.Session;
import com.avijeet.rebase.entities.User;
import com.avijeet.rebase.exceptions.InvalidTokenException;
import com.avijeet.rebase.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
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

        return sessionRepository.save(session);
    }

    @Transactional(readOnly = true)
    public Session getActiveSession(UUID sessionId) {
        Session session = sessionRepository.findByIdAndActiveTrue(sessionId)
                .orElseThrow(() -> new InvalidTokenException("Session does not exist or is inactive"));

        if (session.isExpired()) {
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
    }

    @Transactional
    public void revokeSession(UUID sessionId) {
        sessionRepository.findByIdAndActiveTrue(sessionId).ifPresent(session -> {
            session.setActive(false);
            sessionRepository.save(session);
        });
    }
}

