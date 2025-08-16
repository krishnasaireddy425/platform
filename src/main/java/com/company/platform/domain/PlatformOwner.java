package com.company.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "platform_owners")
@Getter
@Setter
@NoArgsConstructor
public class PlatformOwner {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false, unique = true)
    private String email;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    private String displayName;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}