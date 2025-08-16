package com.company.platform.repo;

import com.company.platform.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface OrganizationRepo extends JpaRepository<Organization, UUID> {
    Optional<Organization> findBySlug(String slug);
}