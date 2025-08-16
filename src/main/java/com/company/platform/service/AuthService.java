package com.company.platform.service;

import com.company.platform.domain.User;
import com.company.platform.err.BadRequestException;
import com.company.platform.repo.UserRepo;
import com.company.platform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Check if user has a temporary password and must change it
        if (u.isMustChangePassword() && u.getTempPasswordHash() != null) {
            // For temporary password users, check against temp password hash
            if (!encoder.matches(password, u.getTempPasswordHash()))
                throw new BadRequestException("Invalid credentials");
        } else {
            // For regular users, check against regular password hash
            if (!encoder.matches(password, u.getPasswordHash()))
                throw new BadRequestException("Invalid credentials");
        }

        return jwt.generateAccessTokenForUser(u.getId(), u.getEmail());
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User u = users.findByEmailIgnoreCase(email).orElseThrow(() -> new BadRequestException("User not found"));

        // Verify current password (could be temp or regular)
        if (u.isMustChangePassword() && u.getTempPasswordHash() != null) {
            if (!encoder.matches(currentPassword, u.getTempPasswordHash()))
                throw new BadRequestException("Invalid current password");
        } else {
            if (!encoder.matches(currentPassword, u.getPasswordHash()))
                throw new BadRequestException("Invalid current password");
        }

        // Set new password and clear temporary password
        u.setPasswordHash(encoder.encode(newPassword));
        u.setTempPasswordHash(null);
        u.setMustChangePassword(false);
        users.save(u);
    }
}