package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class GoogleSignInFailureException extends AppException {

    public GoogleSignInFailureException() {
        super("Google sign-in failed. Please try again.", HttpStatus.UNAUTHORIZED);
    }
}
