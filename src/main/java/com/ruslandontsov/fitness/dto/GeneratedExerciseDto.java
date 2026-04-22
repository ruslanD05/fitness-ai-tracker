package com.ruslandontsov.fitness.dto;

public record GeneratedExerciseDto(
        Long exerciseTypeId,
        String exerciseName,
        int sets,
        double suggestedWeight,
        int targetReps,
        int restSeconds,
        String progressionReason
) {}