package com.ruslandontsov.fitness.dto;

import com.ruslandontsov.fitness.model.MuscleGroup;

import java.util.List;

public record AiWorkoutResponse(
        List<MuscleGroup> selectedMuscleGroups,
        int durationMinutes,
        int estimatedDurationSeconds,
        List<GeneratedExerciseDto> exercises,
        String aiSummary,
        String warning
) {}
