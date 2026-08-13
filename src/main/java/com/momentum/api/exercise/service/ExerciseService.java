package com.momentum.api.exercise.service;

import com.momentum.api.exercise.dto.request.ExerciseRequest;
import com.momentum.api.exercise.dto.response.ExerciseResponse;
import com.momentum.api.exercise.enums.Difficulty;
import com.momentum.api.exercise.enums.Equipment;
import com.momentum.api.exercise.enums.MuscleGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ExerciseService {

    Page<ExerciseResponse> getExercises(
            MuscleGroup muscleGroup,
            Equipment equipment,
            Difficulty difficulty,
            String search,
            Pageable pageable);

    ExerciseResponse createExercise(ExerciseRequest requestPayload);

    ExerciseResponse getExerciseById(UUID id);
}
