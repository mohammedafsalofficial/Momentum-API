package com.momentum.api.app.dto.request;

import com.momentum.api.app.enums.Difficulty;
import com.momentum.api.app.enums.Equipment;
import com.momentum.api.app.enums.MuscleGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be at most 150 characters")
    private String name;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    private String description;

    @NotNull(message = "Muscle group is required")
    private MuscleGroup muscleGroup;

    @NotNull(message = "Equipment is required")
    private Equipment equipment;

    @NotNull(message = "Difficulty is required")
    private Difficulty difficulty;

    private String instructions;

    private String videoUrl;
}
