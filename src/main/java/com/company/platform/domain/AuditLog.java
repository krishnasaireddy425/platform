package com.company.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_org_created", columnList = "org_id,created_at"),
        @Index(name = "idx_audit_actor_created", columnList = "actor_user_id,created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "org_id")
    private UUID orgId;
    @Column(name = "actor_user_id")
    private UUID actorUserId;
    @Column(nullable = false)
    private String action;
    @Column(name = "target_type")
    private String targetType;
    @Column(name = "target_id")
    private String targetId;
    @Column(columnDefinition = "text")
    private String meta;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}