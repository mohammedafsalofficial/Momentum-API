package com.momentum.api.exercise.dto.response;

import com.momentum.api.exercise.enums.Difficulty;
import com.momentum.api.exercise.enums.Equipment;
import com.momentum.api.exercise.enums.MuscleGroup;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ExerciseResponse {

    private UUID id;
    private String name;
    private String description;
    private MuscleGroup muscleGroup;
    private Equipment equipment;
    private Difficulty difficulty;
    private String instructions;
    private String videoUrl;
    private Instant createdAt;
    private Instant updatedAt;
}
