package com.ruslandontsov.fitness.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
public class JwtService {
    private static final String SECRET = System.getenv("JWT_SECRET");

    public String generateToken(Long userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24)) // 24h
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();
    }

    public Long extractUserId(String token) {
        return Long.parseLong(
                Jwts.parser()
                        .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                        .build()
                        .parseSignedClaims(token)
                        .getPayload()
                        .getSubject()
        );
    }
}
