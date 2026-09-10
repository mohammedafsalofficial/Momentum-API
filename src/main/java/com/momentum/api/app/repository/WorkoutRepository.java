package com.momentum.api.app.repository;

import com.momentum.api.app.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface WorkoutRepository extends JpaRepository<Workout, UUID>, JpaSpecificationExecutor<Workout> {
}
