package com.momentum.api.auth.controller;

import com.momentum.api.auth.dto.request.LoginRequest;
import com.momentum.api.auth.dto.request.RegisterRequest;
import com.momentum.api.auth.dto.response.UserResponse;
import com.momentum.api.auth.service.AuthenticationService;
import com.momentum.api.common.response.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<SuccessResponse<UserResponse>> login(@RequestBody @Valid LoginRequest requestPayload) {
        UserResponse userResponse = authenticationService.login(requestPayload);
        SuccessResponse<UserResponse> responsePayload = SuccessResponse.<UserResponse>builder()
                .success(true)
                .message("User logged in successfully")
                .data(userResponse)
                .build();
        return ResponseEntity.ok(responsePayload);
    }
}
