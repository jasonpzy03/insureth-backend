package com.insureth.auth.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey secretKey;
    private final long expirationMs;

    public JwtService(
            @Value("${security.jwt.secret}") String jwtSecret,
            @Value("${security.jwt.expiration-ms:43200000}") long expirationMs
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.expirationMs = expirationMs;
    }

    public String generateToken(String subject, String role, Long userId) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .claim("userId", userId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(secretKey)
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Instant getExpiryInstant() {
        return Instant.now().plusMillis(expirationMs);
    }
}
