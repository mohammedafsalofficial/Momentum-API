package com.momentum.api.app.service.impl;

import com.momentum.api.app.dto.request.WorkoutRequest;
import com.momentum.api.app.enums.WorkoutStatus;
import com.momentum.api.app.model.Workout;
import com.momentum.api.app.repository.WorkoutRepository;
import com.momentum.api.app.service.WorkoutService;
import com.momentum.api.auth.facade.IAuthenticationFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkoutServiceImpl implements WorkoutService {

    private final IAuthenticationFacade authenticationFacade;
    private final WorkoutRepository workoutRepository;

    @Override
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
