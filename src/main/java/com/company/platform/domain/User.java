package com.company.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    @Column(name = "temp_password_hash")
    private String tempPasswordHash;
    @Column(name = "must_change_password", nullable = false)
    private boolean mustChangePassword = true;
    private String displayName;
    private String avatarUrl;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}