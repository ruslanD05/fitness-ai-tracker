package com.ruslandontsov.fitness.dto;

import com.ruslandontsov.fitness.model.MuscleGroup;

public record MuscleSuggestionDto(
        MuscleGroup muscleGroup,
        String reason
) {}