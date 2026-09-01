package com.momentum.api.app.controller;

import com.momentum.api.app.dto.response.WorkoutResponse;
import com.momentum.api.common.response.ApiResponse;
import com.momentum.api.app.dto.request.WorkoutRequest;
import com.momentum.api.app.model.Workout;
import com.momentum.api.app.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping("/workouts/{id}")
    public ResponseEntity<ApiResponse<?>> getWorkoutById(@PathVariable UUID id) {
        WorkoutResponse dto = workoutService.getWorkoutById(id);
        ApiResponse<?> responseBody = ApiResponse.success("Workout fetched successfully.", dto);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/workouts")
    public ResponseEntity<ApiResponse<?>> createWorkout(@Valid @RequestBody WorkoutRequest requestBody) {
        Workout savedWorkout = workoutService.createWorkout(requestBody);
        ApiResponse<?> responsePayload = ApiResponse.success("Workout saved successfully", savedWorkout);
        return ResponseEntity.ok(responsePayload);
    }
}
