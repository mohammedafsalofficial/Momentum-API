package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailVerificationPendingException extends AppException {

    public EmailVerificationPendingException() {
        super("A verification code has been sent to your email. Please verify your account to continue.", HttpStatus.CONFLICT);
    }
}
