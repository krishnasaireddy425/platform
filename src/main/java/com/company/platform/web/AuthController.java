package com.company.platform.web;

import com.company.platform.service.AuthService;
import com.company.platform.security.CurrentUser;
import com.company.platform.web.dto.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService auth;

    public AuthController(AuthService auth) {
        this.auth = auth;
    }

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest req) {
        var loginResult = auth.login(req.email(), req.password());
        return new TokenResponse(loginResult.getToken(), loginResult.isMustChangePassword());
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            auth.logout(token);
        }
        // Always return success - even if no token provided
    }

    @PostMapping("/change-password")
    public void changePassword(@RequestBody ChangePasswordRequest req, Authentication authentication) {
        CurrentUser cu = (CurrentUser) authentication.getPrincipal();
        auth.changePassword(cu.email(), req.currentPassword(), req.newPassword());
    }
}