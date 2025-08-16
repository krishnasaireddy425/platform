package com.company.platform.service;

import com.company.platform.domain.*;
import com.company.platform.err.BadRequestException;
import com.company.platform.repo.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class InviteService {
    private final InviteRepo invites;
    private final RoleRepo roles;
    private final UserRepo users;
    private final OrgMembershipRepo memberships;
    private final PasswordEncoder encoder;

    public InviteService(InviteRepo invites, RoleRepo roles, UserRepo users,
            OrgMembershipRepo memberships, PasswordEncoder encoder) {
        this.invites = invites;
        this.roles = roles;
        this.users = users;
        this.memberships = memberships;
        this.encoder = encoder;
    }

    @Transactional
    public Invite createInvite(UUID orgId, UUID invitedByUserId, String email, String roleName, String tempPassword,
            int expiresHours) {
        var role = roles.findByNameAndOrgIdIsNull(roleName)
                .orElseThrow(() -> new BadRequestException("Role not found: " + roleName));
        var inv = new Invite();
        inv.setOrgId(orgId);
        inv.setEmail(email.toLowerCase());
        inv.setRole(role);
        inv.setTempPasswordHash(encoder.encode(tempPassword));
        inv.setExpiresAt(Instant.now().plus(expiresHours, ChronoUnit.HOURS));
        inv.setInvitedByUserId(invitedByUserId);
        inv.setStatus("PENDING");
        return invites.save(inv);
    }

    @Transactional
    public void acceptInvite(UUID inviteId, String email, String tempPassword, String newPassword) {
        var inv = invites.findByIdAndStatus(inviteId, "PENDING")
                .orElseThrow(() -> new BadRequestException("Invite not found or not pending"));

        if (!inv.getEmail().equalsIgnoreCase(email))
            throw new BadRequestException("Email mismatch");
        if (inv.getExpiresAt().isBefore(Instant.now()))
            throw new BadRequestException("Invite expired");
        if (!encoder.matches(tempPassword, inv.getTempPasswordHash()))
            throw new BadRequestException("Temp password incorrect");

        // upsert user
        var user = users.findByEmailIgnoreCase(email).orElseGet(() -> {
            var u = new User();
            u.setEmail(email.toLowerCase());
            return u;
        });
        user.setPasswordHash(encoder.encode(newPassword));
        user.setTempPasswordHash(null);
        user.setMustChangePassword(false);
        user = users.save(user);

        // membership
        final UUID finalUserId = user.getId();
        memberships.findByOrgIdAndUserId(inv.getOrgId(), finalUserId).ifPresentOrElse(
                m -> {
                }, // already a member
                () -> {
                    var m = new OrgMembership();
                    m.setOrgId(inv.getOrgId());
                    m.setUserId(finalUserId);
                    m.setRole(inv.getRole());
                    memberships.save(m);
                });

        inv.setAcceptedAt(Instant.now());
        inv.setStatus("ACCEPTED");
        invites.save(inv);
    }
}