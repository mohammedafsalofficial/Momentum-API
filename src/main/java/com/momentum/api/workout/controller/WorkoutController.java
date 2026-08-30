package com.momentum.api.workout.controller;

import com.momentum.api.common.response.ApiResponse;
import com.momentum.api.workout.dto.request.WorkoutRequest;
import com.momentum.api.workout.model.Workout;
import com.momentum.api.workout.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @PostMapping("/workouts")
    public ResponseEntity<ApiResponse<?>> createWorkout(@Valid @RequestBody WorkoutRequest requestBody) {
        Workout savedWorkout = workoutService.createWorkout(requestBody);
        ApiResponse<?> responsePayload = ApiResponse.success("Workout saved successfully", savedWorkout);
        return ResponseEntity.ok(responsePayload);
    }
}
