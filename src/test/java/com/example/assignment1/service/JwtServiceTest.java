package com.example.assignment1.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.Key;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    private JwtService jwtService;
    private final String SECRET_KEY = "secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm";

    @BeforeEach
    public void setUp() {
        jwtService = new JwtService();
    }

    @Test
    public void testGenerateToken() {
        String username = "testuser";
        String role = "TEACHER";

        String token = jwtService.generateToken(username, role);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.contains("."));
    }

    @Test
    public void testExtractJwtData() {
        String username = "testuser";
        String role = "STUDENT";

        String token = jwtService.generateToken(username, role);
        JwtService.JwtData jwtData = jwtService.extractJwtData(token);

        assertNotNull(jwtData);
        assertEquals(username, jwtData.username);
        assertEquals(role, jwtData.role);
    }

    @Test
    public void testGenerateAndExtractMultipleTokens() {
        String[] usernames = {"alice", "bob", "charlie"};
        String[] roles = {"TEACHER", "STUDENT", "TEACHER"};

        for (int i = 0; i < usernames.length; i++) {
            String token = jwtService.generateToken(usernames[i], roles[i]);
            JwtService.JwtData jwtData = jwtService.extractJwtData(token);

            assertEquals(usernames[i], jwtData.username);
            assertEquals(roles[i], jwtData.role);
        }
    }

    @Test
    public void testTokenWithSpecialCharactersInUsername() {
        String username = "user@example.com";
        String role = "ADMIN";

        String token = jwtService.generateToken(username, role);
        JwtService.JwtData jwtData = jwtService.extractJwtData(token);

        assertEquals(username, jwtData.username);
        assertEquals(role, jwtData.role);
    }

    @Test
    public void testInvalidTokenThrowsException() {
        String invalidToken = "invalid.token.here";

        assertThrows(Exception.class, () -> jwtService.extractJwtData(invalidToken));
    }
}
