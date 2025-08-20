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
public class OrgService {
    private final OrganizationRepo orgs;
    private final OrgMembershipRepo memberships;
    private final RoleRepo roles;
    private final UserRepo users;
    private final InviteRepo invites;
    private final PasswordEncoder encoder;
    private final SecureRandom secureRandom = new SecureRandom();

    // Password generation characters (excluding ambiguous characters like 0, O, I,
    // l)
    private static final String PASSWORD_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";

    // Default expiration: 72 hours (3 days)
    private static final int DEFAULT_EXPIRY_HOURS = 72;

    public OrgService(OrganizationRepo orgs, OrgMembershipRepo memberships, RoleRepo roles,
            UserRepo users, InviteRepo invites, PasswordEncoder encoder) {
        this.orgs = orgs;
        this.memberships = memberships;
        this.roles = roles;
        this.users = users;
        this.invites = invites;
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
    public Organization createOrgAsOwner(String name, String slug, UUID ownerUserId) {
        if (orgs.findBySlug(slug).isPresent())
            throw new BadRequestException("Slug already exists");
        var org = new Organization();
        org.setName(name);
        org.setSlug(slug);
        org.setStatus("ACTIVE");
        org = orgs.save(org);

        // NOTE: Platform owner creates the org but is NOT a member
        // The platform owner will create the first actual org owner user next
        // No membership is created here

        return org;
    }

    @Transactional
    public OrgOwnerInviteResponse createOrgOwner(UUID orgId, String email, String displayName) {
        // Check if organization exists
        var org = orgs.findById(orgId)
                .orElseThrow(() -> new BadRequestException("Organization not found"));

        // Check if user with this email already exists
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new BadRequestException("User with this email already exists");
        }

        // Check if there's already a pending invitation for this email in this org
        if (invites.findByEmailAndOrgIdAndStatus(email.toLowerCase(), orgId, "PENDING").isPresent()) {
            throw new BadRequestException("Pending invitation already exists for this email in this organization");
        }

        // Get the OWNER role
        var roleOwner = roles.findByNameAndOrgIdIsNull("OWNER")
                .orElseThrow(() -> new BadRequestException("OWNER role missing"));

        // Generate temporary password automatically
        String tempPassword = generateTempPassword();

        // Create the invitation (similar to regular invites)
        var invite = new Invite();
        invite.setOrgId(orgId);
        invite.setEmail(email.toLowerCase());
        invite.setRole(roleOwner);
        invite.setTempPasswordHash(encoder.encode(tempPassword));
        invite.setExpiresAt(Instant.now().plus(DEFAULT_EXPIRY_HOURS, ChronoUnit.HOURS));
        invite.setInvitedByUserId(null); // Platform owner doesn't have a user ID
        invite.setStatus("PENDING");
        invite = invites.save(invite);

        return new OrgOwnerInviteResponse(invite, tempPassword, displayName);
    }

    /**
     * Response wrapper that includes the invitation, generated temporary password,
     * and display name
     */
    public static class OrgOwnerInviteResponse {
        private final Invite invitation;
        private final String tempPassword;
        private final String displayName;

        public OrgOwnerInviteResponse(Invite invitation, String tempPassword, String displayName) {
            this.invitation = invitation;
            this.tempPassword = tempPassword;
            this.displayName = displayName;
        }

        public Invite getInvitation() {
            return invitation;
        }

        public String getTempPassword() {
            return tempPassword;
        }

        public String getDisplayName() {
            return displayName;
        }
    }
}