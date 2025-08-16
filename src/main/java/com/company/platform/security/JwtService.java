package com.company.platform.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.issuer}")
    private String issuer;
    @Value("${security.jwt.accessTokenTtlMinutes}")
    private long accessTtlMin;

    private SecretKey key() {
        // Always use UTF-8 encoding for simplicity
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(bytes);
    }

    public String generateAccessTokenForUser(UUID userId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userId.toString())
                .issuer(issuer)
                .claim("email", email)
                .claim("subType", "USER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlMin * 60)))
                .signWith(key())
                .compact();
    }

    public String generateAccessTokenForPlatformOwner(UUID ownerId, String email) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(ownerId.toString())
                .issuer(issuer)
                .claim("email", email)
                .claim("subType", "PLATFORM")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlMin * 60)))
                .signWith(key())
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parser().verifyWith(key()).build().parseSignedClaims(token);
    }
}