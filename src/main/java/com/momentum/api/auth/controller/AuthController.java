package com.momentum.api.auth.controller;

import com.momentum.api.auth.dto.request.LoginRequest;
import com.momentum.api.auth.dto.request.RegisterRequest;
import com.momentum.api.auth.dto.response.LoginResponse;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.service.AuthenticationService;
import com.momentum.api.common.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

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
        String accessToken = authenticationService.login(requestPayload);

        ResponseCookie cookie = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(false)  // true in prod
                .path("/")
                .maxAge(Duration.ofHours(1))
                .sameSite("Strict")
                .build();

        LoginResponse responseDto = LoginResponse.builder()
                .accessToken(accessToken)
                .build();

        SuccessResponse<LoginResponse> responsePayload = SuccessResponse.<LoginResponse>builder()
                .success(true)
                .message("User logged in successfully")
                .data(responseDto)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responsePayload);
    }
}
