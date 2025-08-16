package com.company.platform.web;

import com.company.platform.domain.Organization;
import com.company.platform.domain.Invite;
import com.company.platform.domain.User;
import com.company.platform.err.ForbiddenException;
import com.company.platform.security.CurrentUser;
import com.company.platform.service.OrgService;
import com.company.platform.service.PlatformAuthService;
import com.company.platform.service.InviteService;
import com.company.platform.web.dto.CreateOrgRequest;
import com.company.platform.web.dto.CreateInviteRequest;
import com.company.platform.web.dto.CreateOrgOwnerRequest;
import com.company.platform.web.dto.LoginRequest;
import com.company.platform.web.dto.TokenResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/platform")
public class PlatformAdminController {
    private final PlatformAuthService auth;
    private final OrgService orgs;
    private final InviteService invites;

    public PlatformAdminController(PlatformAuthService auth, OrgService orgs, InviteService invites) {
        this.auth = auth;
        this.orgs = orgs;
        this.invites = invites;
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        return new TokenResponse(auth.login(req.email(), req.password()));
    }

    @PostMapping("/orgs")
    public Organization createOrg(Authentication authentication, @RequestBody CreateOrgRequest req) {
        CurrentUser cu = (CurrentUser) authentication.getPrincipal();
        if (cu == null || !cu.isPlatformOwner())
            throw new ForbiddenException("Platform owner token required");
        // For MVP: platform owner creates the org and is NOT a member
        // You'll create the initial OWNER next using /platform/orgs/{orgId}/owner
        var fakeOwner = cu.id(); // if you want to auto-add membership, replace with a real user id
        return orgs.createOrgAsOwner(req.name(), req.slug(), fakeOwner); // or create without owner if you prefer
    }

    @PostMapping("/orgs/{orgId}/owner")
    public User createOrgOwner(@PathVariable UUID orgId,
            @RequestBody CreateOrgOwnerRequest req,
            Authentication authentication) {
        CurrentUser cu = (CurrentUser) authentication.getPrincipal();
        if (cu == null || !cu.isPlatformOwner())
            throw new ForbiddenException("Platform owner token required");

        return orgs.createOrgOwner(orgId, req.email(), req.tempPassword(), req.displayName());
    }

    @PostMapping("/orgs/{orgId}/invites")
    public Invite createInitialInvite(@PathVariable UUID orgId,
            @RequestBody CreateInviteRequest req,
            Authentication authentication) {
        CurrentUser cu = (CurrentUser) authentication.getPrincipal();
        if (cu == null || !cu.isPlatformOwner())
            throw new ForbiddenException("Platform owner token required");

        int hours = req.expiresHours() == null ? 72 : req.expiresHours();
        // Platform owner invites are created with null invitedByUserId since platform
        // owners are not users
        return invites.createInvite(orgId, null, req.email(), req.roleName(), req.tempPassword(), hours);
    }
}