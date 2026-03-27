package com.ruslandontsov.fitness.dto;

import com.ruslandontsov.fitness.model.MuscleGroup;

public record SuggestionResponseDto(
        MuscleGroup suggestedMuscle,
        String reason
) {}