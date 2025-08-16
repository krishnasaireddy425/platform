package com.company.platform.web;

import com.company.platform.security.CurrentUser;
import com.company.platform.service.PlatformAuthService;
import com.company.platform.web.dto.CreateOrgRequest;
import com.company.platform.web.dto.LoginRequest;
import com.company.platform.web.dto.TokenResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class TestController {

    private final PlatformAuthService platformAuthService;

    public TestController(PlatformAuthService platformAuthService) {
        this.platformAuthService = platformAuthService;
    }

    @GetMapping("/test/public")
    public String publicTest() {
        return "Public endpoint working!";
    }

    @PostMapping("/test/platform-login")
    public String testPlatformLogin() {
        return "Platform login endpoint accessible!";
    }

    @PostMapping("/test/platform-auth-login")
    public TokenResponse testPlatformAuthLogin(@RequestBody LoginRequest req) {
        return new TokenResponse("test-token-" + req.email());
    }

    @PostMapping("/test/real-platform-auth")
    public TokenResponse testRealPlatformAuth(@RequestBody LoginRequest req) {
        try {
            String token = platformAuthService.login(req.email(), req.password());
            return new TokenResponse(token);
        } catch (Exception e) {
            return new TokenResponse("ERROR: " + e.getMessage());
        }
    }

    @GetMapping("/test/authenticated")
    public String testAuthenticated(Authentication authentication) {
        if (authentication == null) {
            return "No authentication found";
        }
        CurrentUser user = (CurrentUser) authentication.getPrincipal();
        if (user == null) {
            return "Authentication principal is null";
        }
        return "Authenticated user: " + user.email() + " (type: " + user.subType() + ")";
    }

    @PostMapping("/test/create-org")
    public String testCreateOrg(Authentication authentication, @RequestBody CreateOrgRequest req) {
        try {
            if (authentication == null) {
                return "ERROR: No authentication found";
            }
            CurrentUser cu = (CurrentUser) authentication.getPrincipal();
            if (cu == null) {
                return "ERROR: Authentication principal is null";
            }
            if (!cu.isPlatformOwner()) {
                return "ERROR: Not a platform owner. User type: " + cu.subType();
            }
            return "SUCCESS: Platform owner " + cu.email() + " can create org: " + req.name();
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    @PostMapping("/test/create-org-real")
    public String testCreateOrgReal(Authentication authentication, @RequestBody CreateOrgRequest req) {
        try {
            CurrentUser cu = (CurrentUser) authentication.getPrincipal();
            if (cu == null || !cu.isPlatformOwner()) {
                return "ERROR: Platform owner token required. cu=" + cu + ", isPlatformOwner="
                        + (cu != null && cu.isPlatformOwner());
            }
            // For MVP: platform owner creates the org and is NOT a member
            // You'll invite the initial OWNER next using /orgs/{orgId}/invites
            var fakeOwner = cu.id(); // if you want to auto-add membership, replace with a real user id
            // return orgs.createOrgAsOwner(req.name(), req.slug(), fakeOwner); // or create
            // without owner if you prefer
            return "SUCCESS: Would create org " + req.name() + " with slug " + req.slug() + " for owner " + fakeOwner;
        } catch (Exception e) {
            return "ERROR in create-org-real: " + e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }
}