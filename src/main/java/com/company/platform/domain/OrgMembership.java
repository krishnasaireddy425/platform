package com.company.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "org_memberships", uniqueConstraints = @UniqueConstraint(columnNames = { "org_id", "user_id" }))
@Getter
@Setter
@NoArgsConstructor
public class OrgMembership {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(name = "org_id", nullable = false)
    private UUID orgId;
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(nullable = false)
    private String state = "ACTIVE";
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}