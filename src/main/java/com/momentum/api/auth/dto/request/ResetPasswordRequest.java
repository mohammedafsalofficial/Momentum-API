package com.momentum.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class ResetPasswordRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String resetToken;

    @Size(min = 8, max = 100)
    private String newPassword;
}
