package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidGoogleTokenException extends AppException {

    public InvalidGoogleTokenException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
