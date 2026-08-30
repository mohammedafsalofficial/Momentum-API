package com.momentum.api.app.controller;

import com.momentum.api.common.response.ApiResponse;
import com.momentum.api.app.dto.request.ExerciseRequest;
import com.momentum.api.app.dto.response.ExerciseResponse;
import com.momentum.api.app.enums.Difficulty;
import com.momentum.api.app.enums.Equipment;
import com.momentum.api.app.enums.MuscleGroup;
import com.momentum.api.app.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<ExerciseResponse>>> getExercises(
            @RequestParam(required = false, name = "muscle_group") MuscleGroup muscleGroup,
            @RequestParam(required = false) Equipment equipment,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        Page<ExerciseResponse> exercises = exerciseService.getExercises(muscleGroup, equipment, difficulty, search, pageable);
        ApiResponse<Page<ExerciseResponse>> responsePayload = ApiResponse.success("Exercises fetched successfully", exercises);
        return ResponseEntity.ok(responsePayload);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ExerciseResponse>> getExerciseById(@PathVariable UUID id) {
        ExerciseResponse responseDto = exerciseService.getExerciseById(id);
        ApiResponse<ExerciseResponse> responsePayload =
                ApiResponse.success("Exercise fetched successfully", responseDto);
        return ResponseEntity.ok(responsePayload);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ExerciseResponse>> createExercise(@Valid @RequestBody ExerciseRequest requestPayload) {
        ExerciseResponse responseDto = exerciseService.createExercise(requestPayload);
        ApiResponse<ExerciseResponse> responsePayload =
                ApiResponse.success("Exercise created successfully", responseDto);
        return ResponseEntity.created(URI.create("/api/exercises/" + responseDto.getId())).body(responsePayload);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ExerciseResponse>> updateExercise(@PathVariable UUID id, @Valid @RequestBody ExerciseRequest requestPayload) {
        ExerciseResponse responseDto = exerciseService.updateExercise(id, requestPayload);
        ApiResponse<ExerciseResponse> responsePayload =
                ApiResponse.success("Exercise updated successfully", responseDto);
        return ResponseEntity.ok(responsePayload);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteExerciseById(@PathVariable UUID id) {
        exerciseService.deleteExerciseById(id);
    }
}
