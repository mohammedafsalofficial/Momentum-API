package com.momentum.api.auth.controller;

import com.momentum.api.auth.dto.RegisterRequestDto;
import com.momentum.api.auth.dto.UserResponseDto;
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
    public ResponseEntity<SuccessResponse<UserResponseDto>> register(@RequestBody @Valid RegisterRequestDto requestPayload) {
        UserResponseDto responseDto = authenticationService.register(requestPayload);
        SuccessResponse<UserResponseDto> responsePayload = SuccessResponse.<UserResponseDto>builder()
                .success(true)
                .message("User registered successfully")
                .data(responseDto)
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(responsePayload);
    }
}
