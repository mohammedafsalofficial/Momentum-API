package com.momentum.api.exercise.service;

import com.momentum.api.exercise.dto.request.ExerciseRequest;
import com.momentum.api.exercise.dto.response.ExerciseResponse;

public interface ExerciseService {

    ExerciseResponse createExercise(ExerciseRequest requestPayload);
}
