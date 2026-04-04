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
        boolean straightSets,
        int targetReps,
        String reason
) {
    public boolean isStable() {
        return keepSame || increaseWeight || increaseReps;
    }

    public boolean isStraightSets() {
        return straightSets;
    }
}