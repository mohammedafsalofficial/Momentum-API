package com.momentum.api.auth.service;

import com.momentum.api.auth.enums.IdentityProvider;
import com.momentum.api.auth.exception.InvalidVerificationTokenException;
import com.momentum.api.auth.model.EmailVerificationToken;
import com.momentum.api.auth.model.User;
import com.momentum.api.auth.repository.EmailVerificationTokenRepository;
import com.momentum.api.auth.repository.UserRepository;
import com.momentum.api.auth.util.Constants;
import com.momentum.api.auth.util.TokenGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
 
    /**
     * Creates a fresh token and sends the verification email.
     * Any existing token for this user is deleted first so old links
     * are immediately invalidated.
     */
    @Transactional
    public void createAndSendVerificationToken(User user) {
        emailVerificationTokenRepository.deleteAllByUser(user);

        EmailVerificationToken token = EmailVerificationToken.builder()
                .token(TokenGenerator.generateOtp())
                .user(user)
                .expiresAt(Instant.now().plus(Constants.OTP_TTL))
                .build();

        emailVerificationTokenRepository.save(token);

        // Runs async - registration does not block on SMTP
        emailService.sendEmail(
                user.getEmail(),
                "Your Momentum verification code",
                "Your verification code is:\n\n"
                        + token.getToken() + "\n\n"
                        + "This code expires in 15 minutes.\n"
                        + "If you didn't create an account, you can ignore this email."
        );
    }

    /**
     * Validates the token and flips emailVerified on the User.
     * Token is deleted after use — single-use only.
     */
    @Transactional
    public void verifyEmail(String otp, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(InvalidVerificationTokenException::new);

        EmailVerificationToken token = emailVerificationTokenRepository.findByToken(otp)
                .orElseThrow(InvalidVerificationTokenException::new);

        // Ensure the OTP actually belongs to this user -
        // without this check, any valid OTP could verify any account
        if (!token.getUser().getId().equals(user.getId())) {
            throw new InvalidVerificationTokenException();
        }

        if (token.isExpired()) {
            emailVerificationTokenRepository.delete(token);
            throw new InvalidVerificationTokenException();
        }

        user.markEmailVerified();
        userRepository.save(user);
        emailVerificationTokenRepository.deleteAllByUser(user);  // token is consumed so delete it
    }

    /**
     * Re-sends the verification email. Silently does nothing if:
     * - the email is not registered
     * - the account is already verified
     * - the account belongs to a non-LOCAL provider
     * Always returns the same response to the caller to avoid
     * account enumeration.
     */
    @Transactional
    public void resendVerification(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            boolean isLocal = user.getIdp() == IdentityProvider.LOCAL;
            boolean notYetVerified = !user.isEmailVerified();

            if (isLocal && notYetVerified) {
                createAndSendVerificationToken(user);
            }
        });
    }

    /**
     * Purges expired tokens nightly so they don't accumulate.
     * Cron: every day at 02:00 server time.
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeExpiredToken() {
        log.info("Purging expired email verification tokens");
        emailVerificationTokenRepository.deleteAllExpired(Instant.now());
    }
}
