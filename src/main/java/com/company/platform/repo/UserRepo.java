package com.company.platform.repo;

import com.company.platform.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
}