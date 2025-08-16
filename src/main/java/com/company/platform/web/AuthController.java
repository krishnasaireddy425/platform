package com.company.platform.web;

import com.company.platform.service.AuthService;
import com.company.platform.service.InviteService;
import com.company.platform.web.dto.*;
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

    @PostMapping("/accept-invite")
    public void acceptInvite(@RequestBody AcceptInviteRequest req) {
        invites.acceptInvite(req.inviteId(), req.email(), req.tempPassword(), req.newPassword());
    }
}