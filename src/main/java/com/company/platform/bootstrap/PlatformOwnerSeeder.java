package com.company.platform.bootstrap;

import com.company.platform.domain.PlatformOwner;
import com.company.platform.repo.PlatformOwnerRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PlatformOwnerSeeder {
    @Bean
    CommandLineRunner seedPlatformOwner(PlatformOwnerRepo repo, PasswordEncoder encoder) {
        return args -> {
            String email = System.getenv().getOrDefault("PLATFORM_OWNER_EMAIL", "owner@platform.local");
            String pass = System.getenv().getOrDefault("PLATFORM_OWNER_PASSWORD", "owner123");
            repo.findByEmailIgnoreCase(email).orElseGet(() -> {
                var o = new PlatformOwner();
                o.setEmail(email);
                o.setPasswordHash(encoder.encode(pass));
                o.setDisplayName("Platform Owner");
                return repo.save(o);
            });
        };
    }
}