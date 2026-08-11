package com.momentum.api.exercise.controller;

import com.momentum.api.exercise.dto.request.ExerciseRequest;
import com.momentum.api.exercise.dto.response.ExerciseResponse;
import com.momentum.api.exercise.service.ExerciseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/exercises")
@RequiredArgsConstructor
public class ExerciseController {

    private final ExerciseService exerciseService;

    @GetMapping("/{id}")
    public ResponseEntity<ExerciseResponse> getExerciseById(@PathVariable UUID id) {
        ExerciseResponse responseDto = exerciseService.getExerciseById(id);
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping
    public ResponseEntity<ExerciseResponse> createExercise(@Valid @RequestBody ExerciseRequest requestPayload) {
        ExerciseResponse responseDto = exerciseService.createExercise(requestPayload);
        return ResponseEntity.created(URI.create("/api/exercises/" + responseDto.getId())).body(responseDto);
    }
}
