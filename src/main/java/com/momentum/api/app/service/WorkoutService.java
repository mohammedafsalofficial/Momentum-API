package com.momentum.api.app.service;

import com.momentum.api.app.dto.request.WorkoutRequest;
import com.momentum.api.app.dto.response.WorkoutResponse;
import com.momentum.api.app.enums.WorkoutStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface WorkoutService {

    List<WorkoutResponse> getWorkouts(
            WorkoutStatus status,
            String name,
            Instant startedFrom,
            Instant startedTo,
            Instant completedFrom,
            Instant completedTo
    );

    WorkoutResponse getWorkoutById(UUID id);

    WorkoutResponse createWorkout(WorkoutRequest requestBody);

    WorkoutResponse updateWorkoutStatus(UUID id, WorkoutStatus status);
}
