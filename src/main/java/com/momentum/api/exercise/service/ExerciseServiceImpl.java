package com.momentum.api.exercise.service;

import com.momentum.api.exercise.dto.request.ExerciseRequest;
import com.momentum.api.exercise.dto.response.ExerciseResponse;
import com.momentum.api.exercise.mapper.ExerciseMapper;
import com.momentum.api.exercise.model.Exercise;
import com.momentum.api.exercise.repository.ExerciseRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ExerciseServiceImpl implements ExerciseService {

    private final ExerciseRepository exerciseRepository;
    private final ExerciseMapper exerciseMapper;

    @Override
    public ExerciseResponse createExercise(ExerciseRequest requestPayload) {
        Exercise exercise = exerciseMapper.toEntity(requestPayload);
        Exercise savedExercise = exerciseRepository.save(exercise);
        log.debug(savedExercise.toString());
        return exerciseMapper.toResponse(savedExercise);
    }
}
