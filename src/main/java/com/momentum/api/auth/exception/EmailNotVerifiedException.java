package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailNotVerifiedException extends AppException {

    public EmailNotVerifiedException() {
        super("Email address has not been verified", HttpStatus.UNAUTHORIZED);
    }
}
