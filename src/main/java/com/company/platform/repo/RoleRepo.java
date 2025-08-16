package com.company.platform.repo;

import com.company.platform.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface RoleRepo extends JpaRepository<Role, UUID> {
    Optional<Role> findByNameAndOrgIdIsNull(String name); // OWNER/ADMIN/USER
}