package com.momentum.api.exercise;

import java.util.UUID;

public class ExerciseNotFoundException extends RuntimeException {

    public ExerciseNotFoundException(UUID id) {
        super(String.format("Exercise not found for id: %s", id));
    }
}
