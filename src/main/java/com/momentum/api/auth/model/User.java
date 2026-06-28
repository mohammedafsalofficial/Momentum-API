package com.momentum.api.auth.model;

import com.momentum.api.auth.enums.IdentityProvider;
import com.momentum.api.auth.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = true)
    private String password;

    @Column(nullable = false)
    private String firstName;

    private String lastName;

    private String pictureUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private IdentityProvider idp;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private boolean accountNonLocked;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
        enabled = true;
        accountNonLocked = true;
        role = UserRole.ROLE_USER;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public void markEmailVerified() {
        this.emailVerified = true;
    }

    public void updatePassword(String hashedPassword) {
        this.password = hashedPassword;
    }
}
