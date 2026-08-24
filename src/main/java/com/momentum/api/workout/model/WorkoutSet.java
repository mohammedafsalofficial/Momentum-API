package com.momentum.api.workout.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "workout_sets")
public class WorkoutSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "workout_exercise_id", nullable = false)
    private WorkoutExercise workoutExercise;

    @Column(nullable = false)
    private Integer setNumber;

    @Column(precision = 7, scale = 2)
    private BigDecimal weight;

    private Integer reps;

    private Integer durationSeconds;

    @Column(precision = 8, scale = 2)
    private BigDecimal distance;

    @Column(precision = 3, scale = 1)
    private BigDecimal rpe;

    @Column(nullable = false)
    private Boolean completed = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
