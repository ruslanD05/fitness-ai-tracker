package com.ruslandontsov.fitness.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public class CreateSetEntryRequest {
    @Min(value = 1, message = "Reps must be at least 1")
    public int reps;

    @PositiveOrZero(message = "Weight cannot be negative")
    public double weight;

    @Positive(message = "Exercise type id must be positive")
    public long exerciseTypeId;
}
