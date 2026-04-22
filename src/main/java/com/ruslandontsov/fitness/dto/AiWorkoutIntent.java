package com.ruslandontsov.fitness.dto;

import com.ruslandontsov.fitness.model.MuscleGroup;

import java.util.List;

public record AiWorkoutIntent(
        List<MuscleGroup> preferredMuscleGroups,
        boolean forcePreference,
        String summary
) {}