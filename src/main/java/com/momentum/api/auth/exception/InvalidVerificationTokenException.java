package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationTokenException extends AppException {

    public InvalidVerificationTokenException() {
        super("Verification OTP is invalid or has expired.", HttpStatus.UNAUTHORIZED);
    }
}
