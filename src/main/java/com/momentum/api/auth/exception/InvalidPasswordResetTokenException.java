package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordResetTokenException extends AppException {

    public InvalidPasswordResetTokenException() {
        super("Password reset token is invalid or has expired", HttpStatus.BAD_REQUEST);
    }
}
