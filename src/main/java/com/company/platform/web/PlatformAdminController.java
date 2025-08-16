package com.company.platform.web;

import com.company.platform.domain.Organization;
import com.company.platform.err.ForbiddenException;
import com.company.platform.security.CurrentUser;
import com.company.platform.service.OrgService;
import com.company.platform.service.PlatformAuthService;
import com.company.platform.web.dto.CreateOrgRequest;
import com.company.platform.web.dto.LoginRequest;
import com.company.platform.web.dto.TokenResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/platform")
public class PlatformAdminController {
    private final PlatformAuthService auth;
    private final OrgService orgs;

    public PlatformAdminController(PlatformAuthService auth, OrgService orgs) {
        this.auth = auth;
        this.orgs = orgs;
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
        // You'll invite the initial OWNER next using /orgs/{orgId}/invites
        var fakeOwner = cu.id(); // if you want to auto-add membership, replace with a real user id
        return orgs.createOrgAsOwner(req.name(), req.slug(), fakeOwner); // or create without owner if you prefer
    }
}