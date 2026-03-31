package com.ruslandontsov.fitness.dto;

public record ExerciseProgressionRecommendation(
        Long exerciseTypeId,
        String exerciseName,
        double averageRecentWeight,
        double averageRecentReps,
        double suggestedWeight,
        boolean increaseWeight,
        boolean increaseReps,
        boolean keepSame,
        String targetRepRange,
        String reason
) {}