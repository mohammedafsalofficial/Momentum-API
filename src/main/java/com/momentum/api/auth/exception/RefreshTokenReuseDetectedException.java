package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class RefreshTokenReuseDetectedException extends AppException {

    public RefreshTokenReuseDetectedException() {
        super("Session invalidated due to suspicious activity. Please log in again.", HttpStatus.UNAUTHORIZED);
    }
}
