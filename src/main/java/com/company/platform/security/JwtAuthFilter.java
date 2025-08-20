package com.company.platform.security;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import io.jsonwebtoken.Claims;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwt;
    private final TokenBlacklistService tokenBlacklist;

    public JwtAuthFilter(JwtService jwt, TokenBlacklistService tokenBlacklist) {
        this.jwt = jwt;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);

                // Check if token is blacklisted
                if (tokenBlacklist.isTokenBlacklisted(token)) {
                    // Token is blacklisted - treat as unauthenticated
                    chain.doFilter(req, res);
                    return;
                }

                var jws = jwt.parse(token);
                Claims c = jws.getPayload();
                UUID id = UUID.fromString(c.getSubject());
                String email = c.get("email", String.class);
                String subType = c.get("subType", String.class);
                CurrentUser principal = new CurrentUser(id, email, subType);
                var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ignored) {
                // invalid token -> unauthenticated
            }
        }
        chain.doFilter(req, res);
    }
}