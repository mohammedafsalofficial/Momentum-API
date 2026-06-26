package com.momentum.api.auth.controller;

import com.momentum.api.auth.dto.request.*;
import com.momentum.api.auth.dto.response.LoginResponse;
import com.momentum.api.auth.dto.response.RefreshResponse;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.service.AuthenticationService;
import com.momentum.api.auth.service.EmailVerificationService;
import com.momentum.api.common.response.SuccessResponse;
import com.momentum.api.common.service.CookieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final CookieService cookieService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<UserResponse>> register(@RequestBody @Valid RegisterRequest requestPayload) {
        UserResponse responseDto = authenticationService.register(requestPayload);
        SuccessResponse<UserResponse> responsePayload = SuccessResponse.<UserResponse>builder()
                .success(true)
                .message("Registration successful. Please check your email to verify your account.")
                .data(responseDto)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(responsePayload);
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest requestPayload) {
        AuthenticationService.TokenPair tokens = authenticationService.login(requestPayload);
        LoginResponse responseDto = LoginResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .build();
        SuccessResponse<LoginResponse> responsePayload = SuccessResponse.<LoginResponse>builder()
                .success(true)
                .message("User logged in successfully")
                .data(responseDto)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.buildAccessTokenCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.buildRefreshTokenCookie(tokens.refreshToken()).toString())
                .body(responsePayload);
    }

    @PostMapping("/google")
    public ResponseEntity<SuccessResponse<LoginResponse>> googleSignIn(@RequestBody @Valid GoogleSignInRequest requestPayload) {
        AuthenticationService.TokenPair tokens = authenticationService.googleSignIn(requestPayload);
        LoginResponse responseDto = LoginResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .build();
        SuccessResponse<LoginResponse> responsePayload = SuccessResponse.<LoginResponse>builder()
                .success(true)
                .message("User logged in successfully")
                .data(responseDto)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.buildAccessTokenCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.buildRefreshTokenCookie(tokens.refreshToken()).toString())
                .body(responsePayload);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<SuccessResponse<Void>> verifyEmail(@RequestBody @Valid VerifyEmailRequest requestPayload) {
        emailVerificationService.verifyEmail(requestPayload.getOtp(), requestPayload.getEmail());
        SuccessResponse<Void> responsePayload = SuccessResponse.<Void>builder()
                .success(true)
                .message("Email verified successfully. You can now log in now.")
                .build();
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<SuccessResponse<Void>> resendVerification(
            @RequestBody @Valid ResendVerificationRequest requestPayload) {
        emailVerificationService.resendVerification(requestPayload.getEmail());
        // Always the same response regardless of whether email exists/is verified —
        // prevents account enumeration
        SuccessResponse<Void> responsePayload = SuccessResponse.<Void>builder()
                .success(true)
                .message("If that email is registered and unverified, a new link has been sent to your email.")
                .build();
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<RefreshResponse>> refresh(@CookieValue(name = "refresh_token") String refreshToken) {
        AuthenticationService.TokenPair tokens = authenticationService.refresh(refreshToken);
        RefreshResponse responseDto = RefreshResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .build();
        SuccessResponse<RefreshResponse> responsePayload = SuccessResponse.<RefreshResponse>builder()
                .success(true)
                .message("Token refreshed successfully.")
                .data(responseDto)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.buildAccessTokenCookie(tokens.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.buildRefreshTokenCookie(tokens.refreshToken()).toString())
                .body(responsePayload);
    }

    @PostMapping("/logout")
    public ResponseEntity<SuccessResponse<Void>> logout(@CookieValue(name = "refresh_token") String refreshToken) {
        authenticationService.logout(refreshToken);
        SuccessResponse<Void> responsePayload = SuccessResponse.<Void>builder()
                .success(true)
                .message("Logged out successfully.")
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieService.clearAccessTokenCookie().toString())
                .header(HttpHeaders.SET_COOKIE, cookieService.clearRefreshTokenCookie().toString())
                .body(responsePayload);
    }
}
