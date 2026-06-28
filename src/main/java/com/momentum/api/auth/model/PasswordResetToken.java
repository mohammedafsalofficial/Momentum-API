package com.momentum.api.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Represents a short-lived token used during the two-stage password reset flow.
 *
 * <p>This entity serves two sequential purposes — only one token exists per user at a time:
 *
 * <ol>
 *   <li><b>OTP stage</b> — a 6-digit code is stored and emailed to the user to prove
 *       ownership of the account. Expires in 15 minutes.</li>
 *   <li><b>Reset token stage</b> — once the OTP is verified, it is deleted and replaced
 *       with an opaque random token returned to the client. This token authorizes a single
 *       call to the reset-password endpoint. Expires in 10 minutes.</li>
 * </ol>
 *
 * <p>The token is always deleted after use, making it single-use. Any previously issued
 * token for a user is also deleted when a new forgot-password request is made.
 *
 * <p><b>Flow:</b>
 * <pre>
 *   POST /forgot-password      → OTP created and emailed
 *   POST /verify-reset-otp     → OTP deleted, reset token created and returned
 *   POST /reset-password       → reset token deleted, password updated
 * </pre>
 */
@Entity
@Table(name = "password_reset_tokens")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
