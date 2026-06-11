package com.momentum.api.common.exception;

import com.momentum.api.common.response.ErrorResponse;
import com.momentum.api.common.response.FieldError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException exception) {
        ErrorResponse responsePayload = ErrorResponse.builder()
                .success(false)
                .message(exception.getMessage())
                .errors(List.of())
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.status(exception.getStatus()).body(responsePayload);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<FieldError> errors = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();

        ErrorResponse responsePayload = ErrorResponse.builder()
                .success(false)
                .message("Validation failed")
                .errors(errors)
                .timestamp(Instant.now())
                .build();

        return ResponseEntity.badRequest().body(responsePayload);
    }
}
