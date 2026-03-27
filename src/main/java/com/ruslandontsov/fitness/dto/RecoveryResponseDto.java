package com.ruslandontsov.fitness.dto;

import com.ruslandontsov.fitness.model.MuscleGroup;

public record RecoveryResponseDto(
        MuscleGroup muscleGroup,
        double fatigueScore,
        String status
) {}