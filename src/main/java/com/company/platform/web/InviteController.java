package com.company.platform.web;

import com.company.platform.err.ForbiddenException;
import com.company.platform.guard.OrgGuard;
import com.company.platform.security.CurrentUser;
import com.company.platform.service.InviteService;
import com.company.platform.web.dto.CreateInviteRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/orgs/{orgId}/invites")
public class InviteController {
    private final InviteService invites;
    private final OrgGuard guard;

    public InviteController(InviteService invites, OrgGuard guard) {
        this.invites = invites;
        this.guard = guard;
    }

    @PostMapping
    public Map<String, Object> createInvite(@PathVariable UUID orgId,
            @RequestBody CreateInviteRequest req,
            Authentication authentication) {
        var cu = (CurrentUser) authentication.getPrincipal();
        if (cu == null || !cu.isUser())
            throw new ForbiddenException("User token required");
        guard.requireRole(cu.id(), orgId, "OWNER", "ADMIN");

        var response = invites.createInvite(orgId, cu.id(), req.email(), req.roleName());

        // Return both invitation details and the generated temporary password
        return Map.of(
                "invitation", response.getInvitation(),
                "tempPassword", response.getTempPassword(),
                "message", "Invitation created successfully. Share the temporary password with the invitee.");
    }
}