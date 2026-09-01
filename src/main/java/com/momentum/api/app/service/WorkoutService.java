package com.momentum.api.app.service;

import com.momentum.api.app.dto.request.WorkoutRequest;
import com.momentum.api.app.dto.response.WorkoutResponse;
import com.momentum.api.app.model.Workout;

import java.util.UUID;

public interface WorkoutService {

    WorkoutResponse getWorkoutById(UUID id);

    Workout createWorkout(WorkoutRequest requestBody);
}
