package com.company.platform.service;

import com.company.platform.domain.PlatformOwner;
import com.company.platform.err.BadRequestException;
import com.company.platform.repo.PlatformOwnerRepo;
import com.company.platform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PlatformAuthService {
    private final PlatformOwnerRepo owners;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public PlatformAuthService(PlatformOwnerRepo owners, PasswordEncoder encoder, JwtService jwt) {
        this.owners = owners;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public String login(String email, String password) {
        PlatformOwner o = owners.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        if (!encoder.matches(password, o.getPasswordHash()))
            throw new BadRequestException("Invalid credentials");
        return jwt.generateAccessTokenForPlatformOwner(o.getId(), o.getEmail());
    }
}