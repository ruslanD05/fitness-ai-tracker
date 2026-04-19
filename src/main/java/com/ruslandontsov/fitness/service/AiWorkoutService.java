package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.AiWorkoutResponse;
import com.ruslandontsov.fitness.dto.GenerateWorkoutRequest;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import com.ruslandontsov.fitness.model.User;
import org.springframework.stereotype.Service;

@Service
public class AiWorkoutService {

    private final WorkoutGenerationService workoutGenerationService;
    private final OpenRouterService openRouterService;

    public AiWorkoutService(
            WorkoutGenerationService workoutGenerationService,
            OpenRouterService openRouterService
    ) {
        this.workoutGenerationService = workoutGenerationService;
        this.openRouterService = openRouterService;
    }

    public AiWorkoutResponse generateWorkout(User user, GenerateWorkoutRequest request) {
        GeneratedWorkoutResponse deterministic =
                workoutGenerationService.generateWorkout(user, request.workoutDuration);

        if (request.textRequest == null || request.textRequest.isBlank()) {
            return toAiResponse(deterministic, "Deterministic workout generated without AI tailoring.", "");
        }

        try {
            return openRouterService.tailorWorkout(deterministic, request.textRequest);
        } catch (Exception e) {
            return toAiResponse(deterministic, "AI tailoring unavailable. Used deterministic workout instead.", "");
        }
    }

    private AiWorkoutResponse toAiResponse(GeneratedWorkoutResponse deterministic, String summary, String warning) {
        return new AiWorkoutResponse(
                deterministic.selectedMuscleGroups(),
                deterministic.durationMinutes(),
                deterministic.estimatedDurationSeconds(),
                deterministic.exercises(),
                summary,
                warning
        );
    }
}