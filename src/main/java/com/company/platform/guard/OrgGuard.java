package com.company.platform.guard;

import com.company.platform.domain.OrgMembership;
import com.company.platform.err.ForbiddenException;
import com.company.platform.repo.OrgMembershipRepo;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class OrgGuard {
    private final OrgMembershipRepo memberships;

    public OrgGuard(OrgMembershipRepo memberships) {
        this.memberships = memberships;
    }

    public OrgMembership requireRole(UUID userId, UUID orgId, String... allowedRoles) {
        var m = memberships.findByOrgIdAndUserId(orgId, userId)
                .orElseThrow(() -> new ForbiddenException("Not a member of this organization"));
        String role = m.getRole().getName();
        boolean ok = Arrays.stream(allowedRoles).anyMatch(role::equals);
        if (!ok)
            throw new ForbiddenException("Insufficient role");
        return m;
    }
}