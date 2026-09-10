package com.momentum.api.app.specification;

import com.momentum.api.app.enums.WorkoutStatus;
import com.momentum.api.app.model.Workout;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;

public class WorkoutSpecification {

    private WorkoutSpecification() {}

    public static Specification<Workout> hasStatus(WorkoutStatus workoutStatus) {
        return ((root, query, cb) -> workoutStatus == null ? null : cb.equal(root.get("status"), workoutStatus));
    }

    public static Specification<Workout> nameContains(String name) {
        return ((root, query, cb) ->
            name == null || name.isBlank()
            ? null
            : cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
        );
    }

    public static Specification<Workout> startedAfter(Instant from) {
        return (root, query, cb) ->
            from == null
            ? null
            : cb.greaterThanOrEqualTo(root.get("startedAt"), from);
    }

    public static Specification<Workout> startedBefore(Instant to) {
        return (root, query, cb) ->
            to == null
            ? null
            : cb.lessThanOrEqualTo(root.get("startedAt"), to);
    }

    public static Specification<Workout> completedAfter(Instant from) {
        return (root, query, cb) ->
            from == null
            ? null
            : cb.greaterThanOrEqualTo(root.get("completedAt"), from);
    }

    public static Specification<Workout> completedBefore(Instant to) {
        return (root, query, cb) ->
            to == null
            ? null
            : cb.lessThanOrEqualTo(root.get("completedAt"), to);
    }

    public static Specification<Workout> filterBy(
            WorkoutStatus status,
            String name,
            Instant startedFrom,
            Instant startedTo,
            Instant completedFrom,
            Instant completedTo
    ) {
        return Specification.where(hasStatus(status))
                .and(nameContains(name))
                .and(startedAfter(startedFrom))
                .and(startedBefore(startedTo))
                .and(completedAfter(completedFrom))
                .and(completedBefore(completedTo));
    }
}
