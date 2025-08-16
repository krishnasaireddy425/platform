package com.company.platform.web;

import com.company.platform.service.AuthService;
import com.company.platform.service.InviteService;
import com.company.platform.security.CurrentUser;
import com.company.platform.web.dto.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;
    private final InviteService invites;

    public AuthController(AuthService auth, InviteService invites) {
        this.auth = auth;
        this.invites = invites;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        String token = auth.login(req.email(), req.password());
        return new TokenResponse(token);
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest req, Authentication authentication) {
        CurrentUser cu = (CurrentUser) authentication.getPrincipal();
        auth.changePassword(cu.email(), req.currentPassword(), req.newPassword());
    }

    @PostMapping("/accept-invite")
    public void acceptInvite(@RequestBody AcceptInviteRequest req) {
        invites.acceptInvite(req.inviteId(), req.email(), req.tempPassword(), req.newPassword());
    }
}