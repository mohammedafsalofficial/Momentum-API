package com.momentum.api.auth.controller;

import com.momentum.api.auth.dto.request.GoogleSignInRequest;
import com.momentum.api.auth.dto.request.LoginRequest;
import com.momentum.api.auth.dto.request.RegisterRequest;
import com.momentum.api.auth.dto.response.LoginResponse;
import com.momentum.api.auth.dto.response.RefreshResponse;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.service.AuthenticationService;
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

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<UserResponse>> register(@RequestBody @Valid RegisterRequest requestPayload) {
        UserResponse responseDto = authenticationService.register(requestPayload);
        SuccessResponse<UserResponse> responsePayload = SuccessResponse.<UserResponse>builder()
                .success(true)
                .message("User registered successfully")
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
