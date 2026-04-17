package com.avijeet.rebase.config.auth;

import com.avijeet.rebase.entities.Session;
import com.avijeet.rebase.exceptions.InvalidTokenException;
import com.avijeet.rebase.service.auth.JwtService;
import com.avijeet.rebase.service.auth.SessionService;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final SessionService sessionService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            String username = jwtService.extractUsername(token);
            String sessionIdRaw = jwtService.extractSessionId(token);

            if (username == null || sessionIdRaw == null || SecurityContextHolder.getContext().getAuthentication() != null) {
                filterChain.doFilter(request, response);
                return;
            }

            Long sessionId = Long.valueOf(sessionIdRaw);
            Session session = sessionService.getActiveSession(sessionId);

            if (!session.getUser().getUsername().equals(username) || !jwtService.isTokenValid(token, username)) {
                throw new InvalidTokenException("Invalid authentication token");
            }

            AuthenticatedUser authenticatedUser = new AuthenticatedUser(
                    username,
                    session.getUser().getId(),
                    sessionId
            );

            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    authenticatedUser,
                    null,
                    Collections.emptyList()
            );
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Authenticated request path={} username={} sessionId={}", request.getRequestURI(), username, sessionId);
        } catch (JwtException | IllegalArgumentException | InvalidTokenException ex) {
            SecurityContextHolder.clearContext();
            log.warn("JWT authentication failed path={} reason={}", request.getRequestURI(), ex.getClass().getSimpleName());
        }

        filterChain.doFilter(request, response);
    }
}

