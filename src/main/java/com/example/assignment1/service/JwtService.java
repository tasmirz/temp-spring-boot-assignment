package com.example.assignment1.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.stereotype.Component;


@Component
public class JwtService {
    public class JwtData {
        public String username;
        public String role;
        public JwtData(String username, String role) {
            this.username = username;
            this.role = role;
        }
    }
    private final Key key = Keys.hmacShaKeyFor("secret-key-that-is-at-least-256-bits-long-for-hs256-algorithm".getBytes(StandardCharsets.UTF_8));

    private final long expirationMillis = 1000 * 60 * 60;

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    public JwtData extractJwtData(String token) {
        JwtData data = new JwtData(
            Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject(),
            Jwts.parser()
                .verifyWith((javax.crypto.SecretKey) key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get("role", String.class)
        );
        return data;
    }
}
