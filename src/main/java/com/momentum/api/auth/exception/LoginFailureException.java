package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class LoginFailureException extends AppException {

    public LoginFailureException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
}
