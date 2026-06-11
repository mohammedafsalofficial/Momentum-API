package com.momentum.api.auth.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class EmailAlreadyExistsException extends AppException {

    public EmailAlreadyExistsException(String email) {
        super(String.format("User with email %s already exists", email), HttpStatus.CONFLICT);
    }
}
