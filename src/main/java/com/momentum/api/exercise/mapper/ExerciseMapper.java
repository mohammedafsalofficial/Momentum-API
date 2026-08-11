package com.momentum.api.exercise.mapper;

import com.momentum.api.exercise.dto.request.ExerciseRequest;
import com.momentum.api.exercise.dto.response.ExerciseResponse;
import com.momentum.api.exercise.model.Exercise;
import org.springframework.stereotype.Component;

@Component
public class ExerciseMapper {

    public Exercise toEntity(ExerciseRequest requestPayload) {
        return Exercise.builder()
                .name(requestPayload.getName())
                .description(requestPayload.getDescription())
                .muscleGroup(requestPayload.getMuscleGroup())
                .equipment(requestPayload.getEquipment())
                .difficulty(requestPayload.getDifficulty())
                .instructions(requestPayload.getInstructions())
                .videoUrl(requestPayload.getVideoUrl())
                .build();
    }

    public ExerciseResponse toResponse(Exercise exercise) {
        return ExerciseResponse.builder()
                .id(exercise.getId())
                .name(exercise.getName())
                .description(exercise.getDescription())
                .muscleGroup(exercise.getMuscleGroup())
                .equipment(exercise.getEquipment())
                .difficulty(exercise.getDifficulty())
                .instructions(exercise.getInstructions())
                .videoUrl(exercise.getVideoUrl())
                .createdAt(exercise.getCreatedAt())
                .updatedAt(exercise.getUpdatedAt())
                .build();
    }
}