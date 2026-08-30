package com.momentum.api.app.dto.request;

import com.momentum.api.validator.EnumValidator;
import com.momentum.api.app.enums.WorkoutStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record WorkoutRequest(
        @NotBlank(message = "workout name is required")
        @Size(min = 2, message = "workout name should be at least 2 characters")
        @Size(max = 100, message = "workout name should not exceed 100 characters")
        String name,

        @NotBlank(message = "Status is required")
        @EnumValidator(enumClazz = WorkoutStatus.class, message = "Status must be one of: IN_PROGRESS, COMPLETED OR CANCELLED")
        String status,

        @NotNull(message = "startedAt timestamp is required")
        @PastOrPresent(message = "startedAt must be a current or past timestamp")
        Instant startedAt,

        Instant completedAt,

        String notes
) {
}
