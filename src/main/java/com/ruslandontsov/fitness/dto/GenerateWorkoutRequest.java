package com.ruslandontsov.fitness.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class GenerateWorkoutRequest {
    @Positive
    public Long workoutDuration;
    @NotNull
    public boolean includeWarmup;
}
