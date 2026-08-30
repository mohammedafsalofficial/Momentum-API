package com.momentum.api.app.service;

import com.momentum.api.app.dto.request.ExerciseRequest;
import com.momentum.api.app.dto.response.ExerciseResponse;
import com.momentum.api.app.enums.Difficulty;
import com.momentum.api.app.enums.Equipment;
import com.momentum.api.app.enums.MuscleGroup;
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

    ExerciseResponse updateExercise(UUID id, ExerciseRequest requestPayload);

    void deleteExerciseById(UUID id);
}
