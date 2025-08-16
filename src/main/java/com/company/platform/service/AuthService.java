package com.company.platform.service;

import com.company.platform.domain.User;
import com.company.platform.err.BadRequestException;
import com.company.platform.repo.UserRepo;
import com.company.platform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final UserRepo users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepo users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public String login(String email, String password) {
        User u = users.findByEmailIgnoreCase(email).orElseThrow(() -> new BadRequestException("Invalid credentials"));
        if (!encoder.matches(password, u.getPasswordHash()))
            throw new BadRequestException("Invalid credentials");
        return jwt.generateAccessTokenForUser(u.getId(), u.getEmail());
    }
}