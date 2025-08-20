package com.company.platform.security;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TokenBlacklistService {

    private final JwtService jwtService;

    // In-memory storage for blacklisted tokens (for MVP)
    // In production, you might want to use Redis or database storage
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    public TokenBlacklistService(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /**
     * Add a token to the blacklist
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);

        // Optional: Clean up expired tokens periodically to prevent memory leaks
        cleanupExpiredTokens();
    }

    /**
     * Check if a token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Remove expired tokens from blacklist to save memory
     * This is called automatically when blacklisting tokens
     */
    private void cleanupExpiredTokens() {
        try {
            blacklistedTokens.removeIf(token -> {
                try {
                    var jws = jwtService.parse(token);
                    Claims claims = jws.getPayload();
                    Date expiration = claims.getExpiration();
                    return expiration.before(Date.from(Instant.now()));
                } catch (Exception e) {
                    // If token is invalid/expired, remove it from blacklist
                    return true;
                }
            });
        } catch (Exception e) {
            // Ignore cleanup errors - they're not critical
        }
    }

    /**
     * Get the count of blacklisted tokens (for monitoring)
     */
    public int getBlacklistedTokenCount() {
        return blacklistedTokens.size();
    }

    /**
     * Clear all blacklisted tokens (mainly for testing)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
    }
}