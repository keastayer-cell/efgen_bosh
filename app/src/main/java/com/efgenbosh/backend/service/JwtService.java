package com.efgenbosh.backend.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expiresMinutes;

    public JwtService(
        @Value("${JWT_SECRET:}") String jwtSecret,
        @Value("${JWT_EXPIRES_MINUTES:480}") long expiresMinutes
    ) {
        String normalizedSecret = String.valueOf(jwtSecret == null ? "" : jwtSecret).trim();
        if (normalizedSecret.length() < 32) {
            throw new IllegalStateException(
                "JWT_SECRET должен быть задан через окружение и содержать не менее 32 символов."
            );
        }
        this.secretKey = Keys.hmacShaKeyFor(
            normalizedSecret.getBytes(StandardCharsets.UTF_8)
        );
        this.expiresMinutes = expiresMinutes;
    }

    public String generateToken(
        Long userId,
        String email,
        String name,
        Integer tokenVersion
    ) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(expiresMinutes, ChronoUnit.MINUTES);

        return Jwts.builder()
            .subject(email)
            .claim("uid", userId)
            .claim("name", name)
            .claim("ver", tokenVersion == null ? 0 : tokenVersion)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiresAt))
            .signWith(secretKey)
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
