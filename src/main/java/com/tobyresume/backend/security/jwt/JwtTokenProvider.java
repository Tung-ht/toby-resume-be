package com.tobyresume.backend.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Generates and validates JWT tokens (HS256). Used after OAuth2 success and on each REST request.
 * JWT secret must be at least 32 bytes (256 bits) per RFC 7518 §3.2 for HMAC-SHA algorithms.
 *
 * @see docs/ai/design/api-design.md §3.2, phase1-mvp §7.2
 */
@Component
public class JwtTokenProvider {

    /** Minimum key size in bytes for HS256 (RFC 7518 §3.2). */
    private static final int MIN_SECRET_BYTES = 32;

    private static final String CLAIM_NAME = "name";
    private static final String CLAIM_PROVIDER = "provider";
    private static final String CLAIM_ROLE = "role";

    private final SecretKey key;
    private final long expirationMs;

    public JwtTokenProvider(
            @Value("${app.security.jwt.secret}") String secret,
            @Value("${app.security.jwt.expiration-ms:86400000}") long expirationMs) {
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (secretBytes.length < MIN_SECRET_BYTES) {
            throw new IllegalStateException(
                    "JWT secret (app.security.jwt.secret / JWT_SECRET) must be at least 32 characters (256 bits) for HMAC-SHA256. "
                            + "Current length: " + secretBytes.length + " bytes. "
                            + "Set JWT_SECRET to a longer value in your environment or application config.");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
        this.expirationMs = expirationMs;
    }

    /**
     * Builds a JWT with sub, name, provider, role, iat, exp.
     */
    public String generateToken(String sub, String name, String provider, String role) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .subject(sub)
                .claim(CLAIM_NAME, name)
                .claim(CLAIM_PROVIDER, provider)
                .claim(CLAIM_ROLE, role != null ? role : "ADMIN")
                .issuedAt(now)
                .expiration(exp)
                .signWith(key)
                .compact();
    }

    /**
     * Parses and validates the token; returns claims or null if invalid/expired.
     */
    public Claims parseAndValidate(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    public String getSubject(Claims claims) {
        return claims.getSubject();
    }

    public String getName(Claims claims) {
        return claims.get(CLAIM_NAME, String.class);
    }

    public String getProvider(Claims claims) {
        return claims.get(CLAIM_PROVIDER, String.class);
    }

    public String getRole(Claims claims) {
        return claims.get(CLAIM_ROLE, String.class);
    }
}
