package com.company.platform.service;

import com.company.platform.domain.*;
import com.company.platform.err.BadRequestException;
import com.company.platform.repo.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrgService {
    private final OrganizationRepo orgs;
    private final OrgMembershipRepo memberships;
    private final RoleRepo roles;
    private final UserRepo users;
    private final PasswordEncoder encoder;

    public OrgService(OrganizationRepo orgs, OrgMembershipRepo memberships, RoleRepo roles,
            UserRepo users, PasswordEncoder encoder) {
        this.orgs = orgs;
        this.memberships = memberships;
        this.roles = roles;
        this.users = users;
        this.encoder = encoder;
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
    public User createOrgOwner(UUID orgId, String email, String tempPassword, String displayName) {
        // Check if organization exists
        var org = orgs.findById(orgId)
                .orElseThrow(() -> new BadRequestException("Organization not found"));

        // Check if user with this email already exists
        if (users.findByEmailIgnoreCase(email).isPresent()) {
            throw new BadRequestException("User with this email already exists");
        }

        // Create the user with temporary password
        var user = new User();
        user.setEmail(email.toLowerCase());
        user.setTempPasswordHash(encoder.encode(tempPassword));
        user.setPasswordHash(encoder.encode("PLACEHOLDER")); // Required field, will be set when user changes password
        user.setMustChangePassword(true);
        user.setDisplayName(displayName);
        user = users.save(user);

        // Create org membership with OWNER role
        var roleOwner = roles.findByNameAndOrgIdIsNull("OWNER")
                .orElseThrow(() -> new BadRequestException("OWNER role missing"));
        var membership = new OrgMembership();
        membership.setOrgId(orgId);
        membership.setUserId(user.getId());
        membership.setRole(roleOwner);
        memberships.save(membership);

        return user;
    }
}