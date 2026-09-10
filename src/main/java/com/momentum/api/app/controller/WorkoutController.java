package com.momentum.api.app.controller;

import com.momentum.api.app.dto.response.WorkoutResponse;
import com.momentum.api.app.enums.WorkoutStatus;
import com.momentum.api.common.response.ApiResponse;
import com.momentum.api.app.dto.request.WorkoutRequest;
import com.momentum.api.app.service.WorkoutService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workouts")
@RequiredArgsConstructor
public class WorkoutController {

    private final WorkoutService workoutService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<WorkoutResponse>>> getWorkouts(
            @RequestParam(required = false) WorkoutStatus status,
            @RequestParam(required = false) String name,
            @RequestParam(required = false  ) Instant startedFrom,
            @RequestParam(required = false) Instant startedTo,
            @RequestParam(required = false) Instant completedFrom,
            @RequestParam(required = false) Instant completedTo
    ) {
        List<WorkoutResponse> dto = workoutService.getWorkouts(
                status,
                name,
                startedFrom,
                startedTo,
                completedFrom,
                completedTo
        );
        ApiResponse<List<WorkoutResponse>> responseBody = ApiResponse.success("Workouts fetched successfully.", dto);
        return ResponseEntity.ok(responseBody);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<WorkoutResponse>> getWorkoutById(@PathVariable UUID id) {
        WorkoutResponse dto = workoutService.getWorkoutById(id);
        ApiResponse<WorkoutResponse> responseBody = ApiResponse.success("Workout fetched successfully.", dto);
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<WorkoutResponse>> createWorkout(@Valid @RequestBody WorkoutRequest requestBody) {
        WorkoutResponse dto = workoutService.createWorkout(requestBody);
        ApiResponse<WorkoutResponse> responsePayload = ApiResponse.success("Workout saved successfully", dto);
        return ResponseEntity.ok(responsePayload);
    }
}
