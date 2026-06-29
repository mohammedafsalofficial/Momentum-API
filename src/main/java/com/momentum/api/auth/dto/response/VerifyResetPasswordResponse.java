package com.momentum.api.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VerifyResetPasswordResponse {

    private String passwordResetToken;
}
