package com.company.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "roles", uniqueConstraints = @UniqueConstraint(columnNames = { "org_id", "name" }))
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    private UUID id;
    @Column(name = "org_id")
    private UUID orgId; // null => system role (OWNER/ADMIN/USER)
    @Column(nullable = false)
    private String name;
    private String description;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
}