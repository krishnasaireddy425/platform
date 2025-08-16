package com.company.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "organizations")
@Getter
@Setter
@NoArgsConstructor
public class Organization {
    @Id
    private UUID id = UUID.randomUUID();
    @Column(nullable = false)
    private String name;
    @Column(unique = true)
    private String slug;
    private String status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();
}