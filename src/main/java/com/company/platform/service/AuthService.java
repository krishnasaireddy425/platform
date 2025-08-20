package com.company.platform.service;

import com.company.platform.domain.User;
import com.company.platform.domain.Invite;
import com.company.platform.domain.OrgMembership;
import com.company.platform.err.BadRequestException;
import com.company.platform.repo.UserRepo;
import com.company.platform.repo.InviteRepo;
import com.company.platform.repo.OrgMembershipRepo;
import com.company.platform.security.JwtService;
import com.company.platform.security.TokenBlacklistService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuthService {
    private final UserRepo users;
    private final InviteRepo invites;
    private final OrgMembershipRepo memberships;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final TokenBlacklistService tokenBlacklist;

    public AuthService(UserRepo users, InviteRepo invites, OrgMembershipRepo memberships,
            PasswordEncoder encoder, JwtService jwt, TokenBlacklistService tokenBlacklist) {
        this.users = users;
        this.invites = invites;
        this.memberships = memberships;
        this.encoder = encoder;
        this.jwt = jwt;
        this.tokenBlacklist = tokenBlacklist;
    }

    @Transactional
    public LoginResult login(String email, String password) {
        // First, try to find existing user
        var existingUser = users.findByEmailIgnoreCase(email);

        if (existingUser.isPresent()) {
            // User exists - normal login flow
            User user = existingUser.get();

            // Check if user has a temporary password and must change it
            if (user.isMustChangePassword() && user.getTempPasswordHash() != null) {
                // For temporary password users, check against temp password hash
                if (!encoder.matches(password, user.getTempPasswordHash()))
                    throw new BadRequestException("Invalid credentials");
            } else {
                // For regular users, check against regular password hash
                if (!encoder.matches(password, user.getPasswordHash()))
                    throw new BadRequestException("Invalid credentials");
            }

            String token = jwt.generateAccessTokenForUser(user.getId(), user.getEmail());
            return new LoginResult(token, user.isMustChangePassword());
        } else {
            // User doesn't exist - check if they have a pending invitation
            var pendingInvite = invites.findByEmailAndStatus(email.toLowerCase(), "PENDING");

            if (pendingInvite.isPresent()) {
                Invite invite = pendingInvite.get();

                // Check if invitation has expired
                if (invite.getExpiresAt().isBefore(Instant.now()))
                    throw new BadRequestException("Invitation has expired");

                // Verify the temp password matches the invitation
                if (!encoder.matches(password, invite.getTempPasswordHash()))
                    throw new BadRequestException("Invalid credentials");

                // Auto-convert invitation to user account
                User newUser = new User();
                newUser.setEmail(email.toLowerCase());
                newUser.setTempPasswordHash(invite.getTempPasswordHash());
                newUser.setPasswordHash(encoder.encode("PLACEHOLDER")); // Will be replaced when they change password
                newUser.setMustChangePassword(true);
                newUser.setDisplayName(email.toLowerCase()); // Default display name to email, they can change it later
                newUser = users.save(newUser);

                // Create org membership
                OrgMembership membership = new OrgMembership();
                membership.setOrgId(invite.getOrgId());
                membership.setUserId(newUser.getId());
                membership.setRole(invite.getRole());
                memberships.save(membership);

                // Mark invitation as accepted
                invite.setAcceptedAt(Instant.now());
                invite.setStatus("ACCEPTED");
                invites.save(invite);

                // Return JWT token for the newly created user (always mustChangePassword=true
                // for new users)
                String token = jwt.generateAccessTokenForUser(newUser.getId(), newUser.getEmail());
                return new LoginResult(token, true);
            } else {
                // No user and no invitation found
                throw new BadRequestException("Invalid credentials");
            }
        }
    }

    public void logout(String token) {
        // Add token to blacklist so it can't be used again
        tokenBlacklist.blacklistToken(token);
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User u = users.findByEmailIgnoreCase(email).orElseThrow(() -> new BadRequestException("User not found"));

        // Verify current password
        if (u.isMustChangePassword() && u.getTempPasswordHash() != null) {
            // For users who must change password, verify against temp password
            if (!encoder.matches(currentPassword, u.getTempPasswordHash()))
                throw new BadRequestException("Current password is incorrect");
        } else {
            // For users with regular passwords, verify against regular password hash
            if (!encoder.matches(currentPassword, u.getPasswordHash()))
                throw new BadRequestException("Current password is incorrect");
        }

        // Update to new password
        u.setPasswordHash(encoder.encode(newPassword));
        u.setTempPasswordHash(null); // Clear temp password
        u.setMustChangePassword(false); // No longer needs to change password
        users.save(u);
    }

    /**
     * Result of login containing both token and password change requirement
     */
    public static class LoginResult {
        private final String token;
        private final boolean mustChangePassword;

        public LoginResult(String token, boolean mustChangePassword) {
            this.token = token;
            this.mustChangePassword = mustChangePassword;
        }

        public String getToken() {
            return token;
        }

        public boolean isMustChangePassword() {
            return mustChangePassword;
        }
    }
}