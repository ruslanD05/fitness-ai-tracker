package com.ruslandontsov.fitness.dto;

import com.ruslandontsov.fitness.model.MuscleGroup;

import java.util.List;

public record WorkoutIntent(
        Mode mode,
        List<MuscleGroup> requestedMuscleGroups  // only populated when mode == MUSCLE_CHANGE
) {
    public enum Mode {
        MUSCLE_CHANGE,  // user wants different muscle groups
        TWEAK           // user wants to adjust exercises, weight, sets, etc.
    }
}
