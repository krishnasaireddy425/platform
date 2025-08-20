package com.company.platform.repo;

import com.company.platform.domain.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface InviteRepo extends JpaRepository<Invite, UUID> {
    Optional<Invite> findByIdAndStatus(UUID id, String status);

    Optional<Invite> findByEmailAndStatus(String email, String status);

    Optional<Invite> findByEmailAndOrgIdAndStatus(String email, UUID orgId, String status);
}