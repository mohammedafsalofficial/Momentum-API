package com.momentum.api.workout.controller;

import com.momentum.api.common.response.ApiResponse;
import com.momentum.api.workout.dto.request.WorkoutRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class WorkoutController {

    @PostMapping("/workouts")
    public ResponseEntity<ApiResponse<?>> createWorkout(@Valid @RequestBody WorkoutRequest requestPayload) {
        ApiResponse<?> responsePayload = ApiResponse.success("");
        return ResponseEntity.ok(responsePayload);
    }
}
