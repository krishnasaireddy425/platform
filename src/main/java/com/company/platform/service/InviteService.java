package com.company.platform.service;

import com.company.platform.domain.*;
import com.company.platform.err.BadRequestException;
import com.company.platform.repo.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class InviteService {
    private final InviteRepo invites;
    private final RoleRepo roles;
    private final PasswordEncoder encoder;
    private final SecureRandom secureRandom = new SecureRandom();

    // Default expiration: 72 hours (3 days)
    private static final int DEFAULT_EXPIRY_HOURS = 72;

    // Password generation characters (excluding ambiguous characters like 0, O, I,
    // l)
    private static final String PASSWORD_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    public InviteService(InviteRepo invites, RoleRepo roles, PasswordEncoder encoder) {
        this.invites = invites;
        this.roles = roles;
        this.encoder = encoder;
    }

    /**
     * Generates a secure random temporary password
     */
    private String generateTempPassword() {
        StringBuilder password = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            password.append(PASSWORD_CHARS.charAt(secureRandom.nextInt(PASSWORD_CHARS.length())));
        }
        return password.toString();
    }

    @Transactional
    public InviteResponse createInvite(UUID orgId, UUID invitedByUserId, String email, String roleName) {
        var role = roles.findByNameAndOrgIdIsNull(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));

        // Generate temporary password automatically
        String tempPassword = generateTempPassword();

        var inv = new Invite();
        inv.setOrgId(orgId);
        inv.setEmail(email.toLowerCase());
        inv.setRole(role);
        inv.setTempPasswordHash(encoder.encode(tempPassword));
        inv.setExpiresAt(Instant.now().plus(DEFAULT_EXPIRY_HOURS, ChronoUnit.HOURS));
        inv.setInvitedByUserId(invitedByUserId);
        inv.setStatus("PENDING");
        inv = invites.save(inv);

        // Return both the invitation and the plain text password for the response
        return new InviteResponse(inv, tempPassword);
    }

    /**
     * Response wrapper that includes both the invitation and the generated
     * temporary password
     */
    public static class InviteResponse {
        private final Invite invitation;
        private final String tempPassword;

        public InviteResponse(Invite invitation, String tempPassword) {
            this.invitation = invitation;
            this.tempPassword = tempPassword;
        }

        public Invite getInvitation() {
            return invitation;
        }

        public String getTempPassword() {
            return tempPassword;
        }
    }
}