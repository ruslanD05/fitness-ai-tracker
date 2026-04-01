package com.ruslandontsov.fitness.dto;

import com.ruslandontsov.fitness.model.MuscleGroup;

import java.util.List;

public record SuggestionResponseDto(
        List<MuscleSuggestionDto> suggestions
) {}