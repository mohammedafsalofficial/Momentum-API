package com.momentum.api.exercise.service;

import com.momentum.api.exercise.dto.request.ExerciseRequest;
import com.momentum.api.exercise.dto.response.ExerciseResponse;

import java.util.UUID;

public interface ExerciseService {

    ExerciseResponse createExercise(ExerciseRequest requestPayload);

    ExerciseResponse getExerciseById(UUID id);
}
