package com.momentum.api.app.mapper;

import com.momentum.api.app.dto.response.WorkoutResponse;
import com.momentum.api.app.model.Workout;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface WorkoutMapper {

    WorkoutResponse toResponse(Workout workout);
}
