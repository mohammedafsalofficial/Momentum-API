package com.momentum.api.app.exception;

import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class ExerciseNotFoundException extends AppException {

    public ExerciseNotFoundException(UUID id) {
        super(String.format("Exercise not found for id: %s", id), HttpStatus.NOT_FOUND);
    }
}
