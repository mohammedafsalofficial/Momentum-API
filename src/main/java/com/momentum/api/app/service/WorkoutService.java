package com.momentum.api.app.service;

import com.momentum.api.app.dto.request.WorkoutRequest;
import com.momentum.api.app.model.Workout;

public interface WorkoutService {

    Workout createWorkout(WorkoutRequest requestBody);
}
