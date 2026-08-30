package com.momentum.api.workout.service;

import com.momentum.api.auth.facade.IAuthenticationFacade;
import com.momentum.api.workout.dto.request.WorkoutRequest;
import com.momentum.api.workout.enums.WorkoutStatus;
import com.momentum.api.workout.model.Workout;
import com.momentum.api.workout.repository.WorkoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final IAuthenticationFacade authenticationFacade;

    public Workout createWorkout(WorkoutRequest requestBody) {
        Workout workout = Workout.builder()
                .user(authenticationFacade.getAuthenticatedUser())
                .name(requestBody.name())
                .status(WorkoutStatus.valueOf(requestBody.status()))
                .startedAt(requestBody.startedAt())
                .completedAt(requestBody.completedAt())
                .notes(requestBody.notes())
                .build();

        return workoutRepository.save(workout);
    }
}
