package com.momentum.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GoogleSignInRequest {

    @NotBlank(message = "ID token must not be blank")
    private String idToken;
}
