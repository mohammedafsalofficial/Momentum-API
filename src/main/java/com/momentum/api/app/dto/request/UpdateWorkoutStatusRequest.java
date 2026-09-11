package com.momentum.api.app.dto.request;

import com.momentum.api.app.enums.WorkoutStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateWorkoutStatusRequest(
        @NotNull
        WorkoutStatus status
) {
}
