package com.momentum.api.app.specification;

import com.momentum.api.app.enums.Difficulty;
import com.momentum.api.app.enums.Equipment;
import com.momentum.api.app.enums.MuscleGroup;
import com.momentum.api.app.model.Exercise;
import org.springframework.data.jpa.domain.Specification;

public class ExerciseSpecification {

    private ExerciseSpecification() {
    }

    public static Specification<Exercise> hasMuscleGroup(MuscleGroup muscleGroup) {
        return ((root, query, criteriaBuilder) ->
                muscleGroup == null ? null : criteriaBuilder.equal(root.get("muscleGroup"), muscleGroup));
    }

    public static Specification<Exercise> hasEquipment(Equipment equipment) {
        return ((root, query, criteriaBuilder) ->
                equipment == null ? null : criteriaBuilder.equal(root.get("equipment"), equipment));
    }

    public static Specification<Exercise> hasDifficulty(Difficulty difficulty) {
        return ((root, query, criteriaBuilder) ->
                difficulty == null ? null : criteriaBuilder.equal(root.get("difficulty"), difficulty));
    }

    public static Specification<Exercise> nameContains(String search) {
        return ((root, query, criteriaBuilder) ->
                (search == null || search.isBlank()) ? null :
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
    }

    public static Specification<Exercise> filterBy(
            MuscleGroup muscleGroup,
            Equipment equipment,
            Difficulty difficulty,
            String search
    ) {
        return Specification.where(hasMuscleGroup(muscleGroup))
                .and(hasEquipment(equipment))
                .and(hasDifficulty(difficulty))
                .and(nameContains(search));
    }
}
