package com.company.platform.repo;

import com.company.platform.domain.OrgMembership;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OrgMembershipRepo extends JpaRepository<OrgMembership, UUID> {
    Optional<OrgMembership> findByOrgIdAndUserId(UUID orgId, UUID userId);

    List<OrgMembership> findByUserId(UUID userId);
}