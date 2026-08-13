package com.momentum.api.exercise.service;

import com.momentum.api.exercise.enums.Difficulty;
import com.momentum.api.exercise.enums.Equipment;
import com.momentum.api.exercise.enums.MuscleGroup;
import com.momentum.api.exercise.exception.ExerciseNotFoundException;
import com.momentum.api.exercise.dto.request.ExerciseRequest;
import com.momentum.api.exercise.dto.response.ExerciseResponse;
import com.momentum.api.exercise.mapper.ExerciseMapper;
import com.momentum.api.exercise.model.Exercise;
import com.momentum.api.exercise.repository.ExerciseRepository;
import com.momentum.api.exercise.specification.ExerciseSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    @Override
    public Page<ExerciseResponse> getExercises(
            MuscleGroup muscleGroup,
            Equipment equipment,
            Difficulty difficulty,
            String search,
            Pageable pageable) {
        var spec = ExerciseSpecification.filterBy(muscleGroup, equipment, difficulty, search);
        return exerciseRepository.findAll(spec, pageable).map(exerciseMapper::toResponse);
    }

    @Override
    public ExerciseResponse createExercise(ExerciseRequest requestPayload) {
        Exercise exercise = exerciseMapper.toEntity(requestPayload);
        Exercise savedExercise = exerciseRepository.save(exercise);
        return exerciseMapper.toResponse(savedExercise);
    }

    @Override
    public ExerciseResponse getExerciseById(UUID id) {
        Exercise exercise = findExerciseOrThrow(id);
        return exerciseMapper.toResponse(exercise);
    }

    private Exercise findExerciseOrThrow(UUID id) {
        return exerciseRepository.findById(id)
                .orElseThrow((() -> new ExerciseNotFoundException(id)));
    }
}
