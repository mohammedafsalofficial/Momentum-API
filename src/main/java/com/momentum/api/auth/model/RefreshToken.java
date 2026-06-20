package com.momentum.api.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    // Groups all tokens in one rotation chain — same value across the whole chain
    @Column(nullable = false, updatable = false)
    private String familyId;

    // True once this token has been used to rotate into a new one
    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        if (familyId == null) {
            familyId = UUID.randomUUID().toString();
        }
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
