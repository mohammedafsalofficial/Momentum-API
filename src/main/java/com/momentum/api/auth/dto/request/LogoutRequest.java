package com.momentum.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class LogoutRequest {

    @NotBlank
    private String accessToken;

    @NotBlank
    private String refreshToken;
}
