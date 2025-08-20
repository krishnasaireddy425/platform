package com.company.platform.web;

import com.company.platform.domain.Organization;
import com.company.platform.domain.Invite;
import com.company.platform.domain.User;
import com.company.platform.err.ForbiddenException;
import com.company.platform.security.CurrentUser;
import com.company.platform.security.TokenBlacklistService;
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

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;
import java.util.Map;

@RestController
@RequestMapping("/platform")
public class PlatformAdminController {
    private final PlatformAuthService auth;
    private final OrgService orgs;
    private final InviteService invites;
    private final TokenBlacklistService tokenBlacklist;

    public PlatformAdminController(PlatformAuthService auth, OrgService orgs, InviteService invites,
            TokenBlacklistService tokenBlacklist) {
        this.auth = auth;
        this.orgs = orgs;
        this.invites = invites;
        this.tokenBlacklist = tokenBlacklist;
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        return new TokenResponse(auth.login(req.email(), req.password()), false);
    }

    @PostMapping("/auth/logout")
    public void logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            tokenBlacklist.blacklistToken(token);
        }
        // Always return success - even if no token provided
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
    public Map<String, Object> createOrgOwner(@PathVariable UUID orgId,
            @RequestBody CreateOrgOwnerRequest req,
            Authentication authentication) {
        CurrentUser cu = (CurrentUser) authentication.getPrincipal();
        if (cu == null || !cu.isPlatformOwner())
            throw new ForbiddenException("Platform owner token required");

        var response = orgs.createOrgOwner(orgId, req.email(), req.displayName());

        // Return invitation details and the generated temporary password
        return Map.of(
                "invitation", response.getInvitation(),
                "tempPassword", response.getTempPassword(),
                "displayName", response.getDisplayName(),
                "message",
                "Organization owner invitation created successfully. Share the temporary password with the user so they can accept the invitation.");
    }

    @PostMapping("/orgs/{orgId}/invites")
    public Map<String, Object> createInitialInvite(@PathVariable UUID orgId,
            @RequestBody CreateInviteRequest req,
            Authentication authentication) {
        CurrentUser cu = (CurrentUser) authentication.getPrincipal();
        if (cu == null || !cu.isPlatformOwner())
            throw new ForbiddenException("Platform owner token required");

        var response = invites.createInvite(orgId, null, req.email(), req.roleName());

        // Return both invitation details and the generated temporary password
        return Map.of(
                "invitation", response.getInvitation(),
                "tempPassword", response.getTempPassword(),
                "message", "Initial invitation created successfully. Share the temporary password with the invitee.");
    }
}