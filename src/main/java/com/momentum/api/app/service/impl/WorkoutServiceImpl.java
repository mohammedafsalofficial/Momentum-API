package com.momentum.api.app.service.impl;

import com.momentum.api.app.dto.request.WorkoutRequest;
import com.momentum.api.app.dto.response.WorkoutResponse;
import com.momentum.api.app.enums.WorkoutStatus;
import com.momentum.api.app.exception.ResourceNotFoundException;
import com.momentum.api.app.mapper.WorkoutMapper;
import com.momentum.api.app.model.Workout;
import com.momentum.api.app.repository.WorkoutRepository;
import com.momentum.api.app.service.WorkoutService;
import com.momentum.api.app.specification.WorkoutSpecification;
import com.momentum.api.auth.facade.IAuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {

    private final IAuthenticationFacade authenticationFacade;
    private final WorkoutRepository workoutRepository;
    private final WorkoutMapper workoutMapper;

    @Override
    public List<WorkoutResponse> getWorkouts(
            WorkoutStatus status,
            String name,
            Instant startedFrom,
            Instant startedTo,
            Instant completedFrom,
            Instant completedTo
    ) {
        Specification<Workout> specification = WorkoutSpecification.filterBy(
                status,
                name,
                startedFrom,
                startedTo,
                completedFrom,
                completedTo
        );

        return workoutRepository.findAll(specification)
                .stream()
                .map(workoutMapper::toResponse)
                .toList();
    }

    @Override
    public WorkoutResponse getWorkoutById(UUID id) {
        Workout workout = workoutRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException(String.format("Workout not found for id: %s", id)));

        return workoutMapper.toResponse(workout);
    }

    @Override
    public WorkoutResponse createWorkout(WorkoutRequest requestBody) {
        Workout workout = Workout.builder()
                .user(authenticationFacade.getAuthenticatedUser())
                .name(requestBody.name())
                .status(WorkoutStatus.valueOf(requestBody.status()))
                .startedAt(requestBody.startedAt())
                .completedAt(requestBody.completedAt())
                .notes(requestBody.notes())
                .build();

        Workout savedWorkout = workoutRepository.save(workout);

        return workoutMapper.toResponse(savedWorkout);
    }
}
