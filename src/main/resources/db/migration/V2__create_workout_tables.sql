-- Table: workouts
CREATE TABLE workouts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workouts_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

-- Table: workout_exercises
CREATE TABLE workout_exercises (
    id BIGSERIAL PRIMARY KEY,
    workout_id UUID NOT NULL,
    exercise_id UUID NOT NULL,
    exercise_order INT NOT NULL,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_workout_exercises_workout FOREIGN KEY (workout_id) REFERENCES workouts (id) ON DELETE CASCADE,
    CONSTRAINT fk_workout_exercises_exercise FOREIGN KEY (exercise_id) REFERENCES exercises (id)
);

-- Table: workout_sets
CREATE TABLE workout_sets (
    id BIGSERIAL PRIMARY KEY,
    workout_exercise_id BIGINT NOT NULL,
    set_number INT NOT NULL,
    weight NUMERIC(7, 2),
    reps INT,
    duration_seconds INT,
    distance NUMERIC(8, 2),
    rpe NUMERIC(3, 1),
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_workout_sets_workout_exercise FOREIGN KEY (workout_exercise_id) REFERENCES workout_exercises (id) ON DELETE CASCADE
);

-- Performance Indexes on Foreign Keys
CREATE INDEX idx_workouts_user_id ON workouts (user_id);
CREATE INDEX idx_workout_exercises_workout_id ON workout_exercises (workout_id);
CREATE INDEX idx_workout_exercises_exercise_id ON workout_exercises (exercise_id);
CREATE INDEX idx_workout_sets_workout_exercise_id ON workout_sets (workout_exercise_id);