package com.company.platform.web;

import com.company.platform.domain.OrgMembership;
import com.company.platform.repo.OrgMembershipRepo;
import com.company.platform.security.CurrentUser;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/me")
public class MeController {
    private final OrgMembershipRepo memberships;

    public MeController(OrgMembershipRepo memberships) {
        this.memberships = memberships;
    }

    @GetMapping("/memberships")
    public List<OrgMembership> myMemberships(Authentication authentication) {
        var cu = (CurrentUser) authentication.getPrincipal();
        return memberships.findByUserId(cu.id());
    }
}