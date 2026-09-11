package com.momentum.api.common.exception;

import com.momentum.api.common.response.ApiResponse;
import com.momentum.api.common.response.FieldError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.InvalidFormatException;

import java.util.Arrays;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse> handleAppException(AppException e) {
        ApiResponse responsePayload = ApiResponse.error(e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(responsePayload);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationException(MethodArgumentNotValidException exception) {
        List<FieldError> errors = exception.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> new FieldError(e.getField(), e.getDefaultMessage()))
                .toList();
        ApiResponse responsePayload = ApiResponse.error("Validation failed", errors);
        return ResponseEntity.badRequest().body(responsePayload);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        String message = "Malformed request body.";

        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException ife) {
            String field = ife.getPath().isEmpty() ? "field" : ife.getPath().get(ife.getPath().size() - 1).getPropertyName();

            if (ife.getTargetType() != null && ife.getTargetType().isEnum()) {
                Object[] allowedValues = ife.getTargetType().getEnumConstants();
                message = "Invalid value '%s' for field '%s'. Allowed values: %s"
                        .formatted(ife.getValue(), field, Arrays.toString(allowedValues));
            } else {
                message = "Invalid value '%s' for field '%s'.".formatted(ife.getValue(), field);
            }
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleException(Exception e) {
        ApiResponse responsePayload = ApiResponse.error("Something went wrong!");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responsePayload);
    }
}
