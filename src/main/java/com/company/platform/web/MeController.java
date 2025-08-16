package com.company.platform.web;

import com.company.platform.domain.OrgMembership;
import com.company.platform.domain.User;
import com.company.platform.repo.OrgMembershipRepo;
import com.company.platform.repo.UserRepo;
import com.company.platform.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/me")
public class MeController {
    private final OrgMembershipRepo memberships;
    private final UserRepo users;

    public MeController(OrgMembershipRepo memberships, UserRepo users) {
        this.memberships = memberships;
        this.users = users;
    }

    @GetMapping("/memberships")
    public List<OrgMembership> myMemberships(Authentication authentication) {
        var cu = (CurrentUser) authentication.getPrincipal();
        return memberships.findByUserId(cu.id());
    }

    @GetMapping("/profile")
    public Map<String, Object> myProfile(Authentication authentication) {
        var cu = (CurrentUser) authentication.getPrincipal();
        User user = users.findById(cu.id()).orElseThrow();

        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "displayName", user.getDisplayName(),
                "mustChangePassword", user.isMustChangePassword(),
                "createdAt", user.getCreatedAt());
    }
}