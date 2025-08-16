package com.company.platform.repo;

import com.company.platform.domain.PlatformOwner;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface PlatformOwnerRepo extends JpaRepository<PlatformOwner, UUID> {
    Optional<PlatformOwner> findByEmailIgnoreCase(String email);
}