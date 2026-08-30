package com.momentum.api.app.service.impl;

import com.momentum.api.app.enums.Difficulty;
import com.momentum.api.app.enums.Equipment;
import com.momentum.api.app.enums.MuscleGroup;
import com.momentum.api.app.exception.ExerciseNotFoundException;
import com.momentum.api.app.dto.request.ExerciseRequest;
import com.momentum.api.app.dto.response.ExerciseResponse;
import com.momentum.api.app.mapper.ExerciseMapper;
import com.momentum.api.app.model.Exercise;
import com.momentum.api.app.repository.ExerciseRepository;
import com.momentum.api.app.service.ExerciseService;
import com.momentum.api.app.specification.ExerciseSpecification;
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

    @Override
    public ExerciseResponse updateExercise(UUID id, ExerciseRequest requestPayload) {
        Exercise exercise = findExerciseOrThrow(id);
        exerciseMapper.updateEntity(exercise, requestPayload);
        Exercise updatedExercise = exerciseRepository.save(exercise);
        return exerciseMapper.toResponse(updatedExercise);
    }

    @Override
    public void deleteExerciseById(UUID id) {
        exerciseRepository.deleteById(id);
    }

    private Exercise findExerciseOrThrow(UUID id) {
        return exerciseRepository.findById(id)
                .orElseThrow((() -> new ExerciseNotFoundException(id)));
    }
}
