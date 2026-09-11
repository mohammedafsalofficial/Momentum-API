package com.momentum.api.app.exception;

import com.momentum.api.app.enums.WorkoutStatus;
import com.momentum.api.common.exception.AppException;
import org.springframework.http.HttpStatus;

public class InvalidWorkoutStatusTransitionException extends AppException {

    public InvalidWorkoutStatusTransitionException(WorkoutStatus currentStatus, WorkoutStatus newStatus) {
        super(
                "Cannot change workout status from %s to %s".formatted(currentStatus, newStatus),
                HttpStatus.BAD_REQUEST
        );
    }
}
