package com.momentum.api.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.momentum.api.auth.dto.request.GoogleSignInRequest;
import com.momentum.api.auth.dto.request.LoginRequest;
import com.momentum.api.auth.dto.request.RegisterRequest;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.enums.IdentityProvider;
import com.momentum.api.auth.exception.EmailAlreadyExistsException;
import com.momentum.api.auth.exception.EmailNotVerifiedException;
import com.momentum.api.auth.exception.LoginFailureException;
import com.momentum.api.auth.model.RefreshToken;
import com.momentum.api.auth.model.User;
import com.momentum.api.auth.repository.UserRepository;
import com.momentum.api.auth.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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

    public UserResponse register(RegisterRequest requestPayload) {
        if (userRepository.existsByEmail(requestPayload.getEmail())) {
            throw new EmailAlreadyExistsException(requestPayload.getEmail());
        }

        User user = User.builder()
                .email(requestPayload.getEmail())
                .password(passwordEncoder.encode(requestPayload.getPassword()))
                .firstName(requestPayload.getFirstName())
                .lastName(requestPayload.getLastName())
                .idp(IdentityProvider.LOCAL)
                .emailVerified(false)
                .build();

        User savedUser = userRepository.save(user);

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

    public TokenPair googleSignIn(GoogleSignInRequest requestPayload) {
        GoogleIdToken.Payload payload = googleTokenVerifierService.verify(requestPayload.getIdToken());

        String email = payload.getEmail();
        String firstName = (String) payload.get("given_name");
        String lastName = (String) payload.get("family_name");
        String picture = (String) payload.get("picture");

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(email)
                            .password(null)
                            .firstName(firstName != null ? firstName : email)
                            .lastName(lastName)
                            .pictureUrl(picture)
                            .idp(IdentityProvider.GOOGLE)
                            .emailVerified(true)
                            .build();
                    return userRepository.save(newUser);
                });

        return generateTokenPair(user);
    }

    public TokenPair refresh(String rawRefreshToken) {
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

    public void logout(String refreshToken) {
        refreshTokenService.deleteByToken(refreshToken);
    }

    /**
     * Simple record to carry both tokens
     */
    public record TokenPair(String accessToken, String refreshToken) {}

    private String generateAccessToken(User user) {
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        return jwtUtil.generateToken(userDetails);
    }

    private TokenPair generateTokenPair(User user) {
        String accessToken = generateAccessToken(user);
        RefreshToken refreshToken = refreshTokenService.create(user);
        return new TokenPair(accessToken, refreshToken.getToken());
    }
}
