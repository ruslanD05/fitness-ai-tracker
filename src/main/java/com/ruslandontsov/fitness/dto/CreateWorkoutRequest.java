package com.ruslandontsov.fitness.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateWorkoutRequest {
    @NotBlank(message = "Workout name is required")
    public String name;
}
