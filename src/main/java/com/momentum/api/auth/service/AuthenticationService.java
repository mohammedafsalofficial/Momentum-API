package com.momentum.api.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.momentum.api.auth.dto.request.*;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.enums.IdentityProvider;
import com.momentum.api.auth.exception.*;
import com.momentum.api.auth.model.PasswordResetToken;
import com.momentum.api.auth.model.RefreshToken;
import com.momentum.api.auth.model.User;
import com.momentum.api.auth.repository.PasswordResetTokenRepository;
import com.momentum.api.auth.repository.UserRepository;
import com.momentum.api.auth.util.Constants;
import com.momentum.api.auth.util.JwtUtil;
import com.momentum.api.auth.util.TokenGenerator;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final GoogleTokenVerifierService googleTokenVerifierService;
    private final CustomUserDetailsService customUserDetailsService;
    private final RefreshTokenService refreshTokenService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * Registers a new LOCAL user account and sends an email verification OTP.
     *
     * <p>If an unverified account already exists for the given email, a fresh OTP is
     * sent and {@link EmailVerificationPendingException} is thrown — this handles the
     * case where a user registered but never verified, then tries to register again.
     *
     * <p>If a verified account already exists for the given email,
     * {@link EmailAlreadyExistsException} is thrown.
     *
     * <p>On success, the account is created with {@code emailVerified = false}.
     * No tokens are issued — the user must verify their email before logging in.
     *
     * @param requestPayload the registration details:
     *                       <ul>
     *                         <li>{@code email} — must be a valid email address</li>
     *                         <li>{@code password} — 8–20 characters, must contain at least
     *                             one uppercase letter, one lowercase letter, one digit,
     *                             and one special character</li>
     *                         <li>{@code firstName} — required, max 50 characters</li>
     *                         <li>{@code lastName} — optional, max 50 characters</li>
     *                       </ul>
     * @return {@link UserResponse} representing the newly created account
     * @throws EmailVerificationPendingException if the email is registered but unverified
     * @throws EmailAlreadyExistsException if the email is already registered and verified
     */
    public UserResponse register(RegisterRequest requestPayload) {
        userRepository.findByEmail(requestPayload.getEmail()).ifPresent((existingUser) -> {
            if (!existingUser.isEmailVerified()) {
                // Account exists but was never verified — resend OTP
                emailVerificationService.createAndSendVerificationToken(existingUser);
                throw new EmailVerificationPendingException();
            }
            throw new EmailAlreadyExistsException(requestPayload.getEmail());
        });

        User newUser = User.builder()
                .email(requestPayload.getEmail())
                .password(passwordEncoder.encode(requestPayload.getPassword()))
                .firstName(requestPayload.getFirstName())
                .lastName(requestPayload.getLastName())
                .idp(IdentityProvider.LOCAL)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(newUser);

        emailVerificationService.createAndSendVerificationToken(savedUser);

        return toUserResponse(savedUser);
    }

    public TokenPair login(LoginRequest requestPayload) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(requestPayload.getEmail(), requestPayload.getPassword())
            );
            CustomUserDetails userDetailsService = (CustomUserDetails) authentication.getPrincipal();
            User user = userDetailsService.getUser();

            if (user.getIdp() == IdentityProvider.LOCAL && !user.isEmailVerified()) {
                throw new EmailNotVerifiedException();
            }

            return generateTokenPair(userDetailsService.getUser());
        } catch (AuthenticationException e) {
            throw new LoginFailureException();
        }
    }

    /**
     * Authenticates a user via Google Sign-In using a Google ID token.
     *
     * <p>If the email from the verified token belongs to an existing account, that account
     * is used directly regardless of how it was originally created. If no account exists,
     * a new GOOGLE account is created with {@code emailVerified = true} — Google has
     * already verified ownership of the email.
     *
     * @param requestPayload contains the Google ID token issued by the client
     * @return {@link TokenPair} containing a fresh access token and refresh token
     * @throws InvalidGoogleTokenException
     *         if the ID token is invalid, expired, or fails verification
     */
    public TokenPair googleSignIn(GoogleSignInRequest requestPayload) {
        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(requestPayload.getIdToken());

        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String pictureUrl = (String) payload.get("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        User.builder()
                                .email(email)
                                .password(null)
                                .firstName(firstName != null ? firstName : email)
                                .lastName(lastName)
                                .pictureUrl(pictureUrl)
                                .idp(IdentityProvider.GOOGLE)
                                .emailVerified(true) // Google already verified ownership of this email
                                .build()
                ));

        return generateTokenPair(user);
    }

    public TokenPair refresh(RefreshRequest requestPayload) {
        String rawRefreshToken = requestPayload.getRefreshToken();
        RefreshToken validated = refreshTokenService.validateAndDetectReuse(rawRefreshToken);
        RefreshToken rotated = refreshTokenService.rotate(validated);
        String accessToken = generateAccessToken(rotated.getUser());
        return new TokenPair(accessToken, rotated.getToken());
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .enabled(user.isEnabled())
                .accountNonLocked(user.isAccountNonLocked())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public void logout(LogoutRequest requestPayload) {
        String token = requestPayload.getAccessToken();
        Claims claims = jwtUtil.extractAllClaimsAllowExpired(token);
        String jti = claims.getId();
        Date tokenExpiration = claims.getExpiration();
        tokenBlacklistService.blacklist(jti, tokenExpiration);
        refreshTokenService.deleteByToken(requestPayload.getRefreshToken());
    }

    public void forgotPassword(ForgotPasswordRequest requestPayload) {
        userRepository.findByEmail(requestPayload.getEmail()).ifPresent((existingUser) -> {
            passwordResetTokenRepository.deleteAllByUser(existingUser);

            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .token(TokenGenerator.generateOtp())
                    .user(existingUser)
                    .expiresAt(Instant.now().plus(Constants.OTP_TTL))
                    .build();

            passwordResetTokenRepository.save(passwordResetToken);

            emailService.sendEmail(
                    existingUser.getEmail(),
                    "Reset your Momentum password",
                    "Your password reset code is:\n\n"
                            + passwordResetToken.getToken() + "\n\n"
                            + "This code expires in 15 minutes.\n"
                            + "If you didn't request a password reset, you can ignore this email."
            );
        });
    }

    public String verifyResetOtp(@Valid VerifyResetOtpRequest requestPayload) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository
                .findByToken(requestPayload.getOtp())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (!passwordResetToken.getUser().getEmail().equals(requestPayload.getEmail())) {
            throw new InvalidPasswordResetTokenException();
        }

        if (passwordResetToken.isExpired()) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new InvalidPasswordResetTokenException();
        }

        // OTP is valid — consume it immediately (single-use)
        passwordResetTokenRepository.delete(passwordResetToken);

        // Issue a dedicated short-lived reset token
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(TokenGenerator.generateResetToken())
                .user(passwordResetToken.getUser())
                .expiresAt(Instant.now().plus(Constants.RESET_TOKEN_TTL))
                .build();

        passwordResetTokenRepository.save(resetToken);

        return resetToken.getToken();
    }

    public void resetPassword(ResetPasswordRequest requestPayload) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(requestPayload.getResetToken())
                .orElseThrow(InvalidPasswordResetTokenException::new);

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            throw new InvalidPasswordResetTokenException();
        }

        User user = resetToken.getUser();
        user.updatePassword(passwordEncoder.encode(requestPayload.getNewPassword()));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);
    }

    /**
     * Carries both tokens issued after a successful authentication.
     *
     * @param accessToken  short-lived JWT used to authorize API requests (15 minutes)
     * @param refreshToken long-lived opaque token used to rotate the access token (30 days)
     */
    public record TokenPair(String accessToken, String refreshToken) {}

    /**
     * Generates a signed JWT access token for the given user.
     *
     * <p>Loads the user's {@link UserDetails} by email to ensure the latest
     * roles and state are reflected in the token.
     *
     * @param user the authenticated user
     * @return a signed JWT access token string
     */
    private String generateAccessToken(User user) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        return jwtUtil.generateToken(userDetails);
    }

    /**
     * Generates a full {@link TokenPair} for the given user.
     *
     * <p>Creates a new refresh token in the database and pairs it with a
     * freshly signed access token.
     *
     * @param user the authenticated user
     * @return {@link TokenPair} containing the access token and refresh token
     */
    private TokenPair generateTokenPair(User user) {
        String accessToken = generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);
        return new TokenPair(accessToken, refreshToken.getToken());
    }
}
