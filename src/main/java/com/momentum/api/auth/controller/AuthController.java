package com.momentum.api.auth.controller;

import com.momentum.api.auth.dto.request.*;
import com.momentum.api.auth.dto.response.LoginResponse;
import com.momentum.api.auth.dto.response.RefreshResponse;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.dto.response.VerifyResetPasswordResponse;
import com.momentum.api.auth.service.AuthenticationService;
import com.momentum.api.auth.service.EmailVerificationService;
import com.momentum.api.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final EmailVerificationService emailVerificationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody @Valid RegisterRequest requestPayload) {
        UserResponse responseDto = authenticationService.register(requestPayload);
        ApiResponse<UserResponse> responsePayload = ApiResponse.success(
                "Registration successful. Please check your email for OTP to verify your account.",
                responseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responsePayload);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest requestPayload) {
        AuthenticationService.TokenPair tokens = authenticationService.login(requestPayload);
        LoginResponse responseDto = LoginResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .build();
        ApiResponse<LoginResponse> responsePayload = ApiResponse.success(
                "User logged in successfully", responseDto);
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<LoginResponse>> googleSignIn(@RequestBody @Valid GoogleSignInRequest requestPayload) {
        AuthenticationService.TokenPair tokens = authenticationService.googleSignIn(requestPayload);
        LoginResponse responseDto = LoginResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .build();
        ApiResponse<LoginResponse> responsePayload = ApiResponse.success(
                "User logged in successfully", responseDto);
        return ResponseEntity.ok(responsePayload);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestBody @Valid VerifyEmailRequest requestPayload) {
        emailVerificationService.verifyEmail(requestPayload.getOtp(), requestPayload.getEmail());
        ApiResponse<Void> responsePayload = ApiResponse.success("Email verified successfully. You can now log in now.");
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @RequestBody @Valid ResendVerificationRequest requestPayload) {
        emailVerificationService.resendVerification(requestPayload.getEmail());
        // Always the same response regardless of whether email exists/is verified —
        // prevents account enumeration
        ApiResponse<Void> responsePayload = ApiResponse
                .success("If that email is registered and unverified, a new OTP has been sent to your email.");
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@RequestBody @Valid RefreshRequest requestPayload) {
        AuthenticationService.TokenPair tokens = authenticationService.refresh(requestPayload);
        RefreshResponse responseDto = RefreshResponse.builder()
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .build();
        ApiResponse<RefreshResponse> responsePayload = ApiResponse.success(
                "Token refreshed successfully.", responseDto);
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestBody @Valid LogoutRequest requestPayload) {
        authenticationService.logout(requestPayload);
        ApiResponse<Void> responsePayload = ApiResponse.success("Logged out successfully.");
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody @Valid ForgotPasswordRequest requestPayload) {
        authenticationService.forgotPassword(requestPayload);
        ApiResponse<Void> responsePayload = ApiResponse
                .success("If that email is registered, a new OTP has been sent to your email.");
        return ResponseEntity.ok(responsePayload);
    }

    @GetMapping("/verify-reset-otp")
    public ResponseEntity<ApiResponse<VerifyResetPasswordResponse>> verifyResetOtp(
            @RequestBody @Valid VerifyResetOtpRequest requestPayload) {
        String resetToken = authenticationService.verifyResetOtp(requestPayload);
        VerifyResetPasswordResponse responseDto = VerifyResetPasswordResponse.builder()
                .passwordResetToken(resetToken)
                .build();
        ApiResponse<VerifyResetPasswordResponse> responsePayload = ApiResponse
                .success("OTP verified successfully. You can now reset your password.", responseDto);
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@RequestBody @Valid ResetPasswordRequest requestPayload) {
        authenticationService.resetPassword(requestPayload);
        ApiResponse<Void> responsePayload = ApiResponse.success("Password reset successfully");
        return ResponseEntity.ok(responsePayload);
    }
}
