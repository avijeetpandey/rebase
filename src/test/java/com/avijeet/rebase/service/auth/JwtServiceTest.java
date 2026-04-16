package com.avijeet.rebase.service.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "0123456789abcdef0123456789abcdef";

    @Test
    void shouldGenerateAndValidateAccessToken() {
        JwtService jwtService = new JwtService(SECRET, 60_000);

        String token = jwtService.generateAccessToken("alice", "b34981d2-c9a8-4fdd-a2f2-7c4c3ac5a5e9");

        assertNotNull(token);
        assertEquals("alice", jwtService.extractUsername(token));
        assertEquals("b34981d2-c9a8-4fdd-a2f2-7c4c3ac5a5e9", jwtService.extractSessionId(token));
        assertTrue(jwtService.isTokenValid(token, "alice"));
    }
}

