package com.momentum.api.app.dto.response;

import com.momentum.api.app.enums.WorkoutStatus;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record WorkoutResponse(
        UUID id,
        String name,
        WorkoutStatus status,
        Instant startedAt,
        Instant completedAt,
        String notes,
        Instant createdAt,
        Instant updatedAt
) {
}
