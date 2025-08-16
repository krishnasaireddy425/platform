package com.company.platform.service;

import com.company.platform.domain.*;
import com.company.platform.err.BadRequestException;
import com.company.platform.repo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OrgService {
    private final OrganizationRepo orgs;
    private final OrgMembershipRepo memberships;
    private final RoleRepo roles;

    public OrgService(OrganizationRepo orgs, OrgMembershipRepo memberships, RoleRepo roles) {
        this.orgs = orgs;
        this.memberships = memberships;
        this.roles = roles;
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

        var roleOwner = roles.findByNameAndOrgIdIsNull("OWNER")
                .orElseThrow(() -> new BadRequestException("OWNER role missing"));
        var m = new OrgMembership();
        m.setOrgId(org.getId());
        m.setUserId(ownerUserId);
        m.setRole(roleOwner);
        memberships.save(m);

        return org;
    }
}