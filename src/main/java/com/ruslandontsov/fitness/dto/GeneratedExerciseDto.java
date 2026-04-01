package com.ruslandontsov.fitness.dto;

public record GeneratedExerciseDto(
        Long exerciseTypeId,
        String exerciseName,
        int sets,
        double suggestedWeight,
        String targetRepRange,
        int restSeconds,
        String progressionReason
) {}