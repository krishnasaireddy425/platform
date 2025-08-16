package com.company.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "invites")
@Getter
@Setter
@NoArgsConstructor
public class Invite {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(name = "org_id", nullable = false)
    private UUID orgId;
    @Column(nullable = false)
    private String email;
    @Column(name = "temp_password_hash", nullable = false)
    private String tempPasswordHash;

    @ManyToOne(optional = false)
    @JoinColumn(name = "role_id")
    private Role role;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;
    @Column(name = "invited_by_user_id")
    private UUID invitedByUserId;
    @Column(name = "accepted_at")
    private Instant acceptedAt;
    @Column(nullable = false)
    private String status = "PENDING";
}