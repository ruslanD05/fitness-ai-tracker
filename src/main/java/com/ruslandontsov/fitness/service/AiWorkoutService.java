package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.AiWorkoutResponse;
import com.ruslandontsov.fitness.dto.ExerciseProgressionRecommendation;
import com.ruslandontsov.fitness.dto.GenerateWorkoutRequest;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import com.ruslandontsov.fitness.dto.WorkoutIntent;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class AiWorkoutService {

    private final WorkoutGenerationService workoutGenerationService;
    private final GeminiService geminiService;

    public AiWorkoutService(
            WorkoutGenerationService workoutGenerationService,
            GeminiService geminiService
    ) {
        this.workoutGenerationService = workoutGenerationService;
        this.geminiService = geminiService;
    }

    public AiWorkoutResponse generateWorkout(User user, GenerateWorkoutRequest request) {
        // No text request — pure deterministic, no Gemini at all
        if (request.textRequest == null || request.textRequest.isBlank()) {
            GeneratedWorkoutResponse deterministic =
                    workoutGenerationService.generateWorkout(user, request.workoutDuration);
            return toAiResponse(deterministic, "Workout generated without AI tailoring.", "");
        }

        try {
            WorkoutIntent intent = geminiService.extractIntent(request.textRequest);

            if (intent.mode() == WorkoutIntent.Mode.MUSCLE_CHANGE
                    && intent.requestedMuscleGroups() != null
                    && !intent.requestedMuscleGroups().isEmpty()) {

                GeneratedWorkoutResponse deterministicForGroups =
                        workoutGenerationService.generateWorkout(
                                user,
                                request.workoutDuration,
                                intent.requestedMuscleGroups()
                        );

                Map<MuscleGroup, List<ExerciseProgressionRecommendation>> catalogue =
                        workoutGenerationService.loadCatalogueForGroups(user, intent.requestedMuscleGroups());

                return geminiService.assembleWorkout(
                        deterministicForGroups,
                        catalogue,
                        request.textRequest,
                        request.workoutDuration
                );

            } else {
                GeneratedWorkoutResponse deterministic =
                        workoutGenerationService.generateWorkout(user, request.workoutDuration);

                return geminiService.tailorWorkout(deterministic, request.textRequest);
            }

        } catch (Exception e) {
            GeneratedWorkoutResponse fallback =
                    workoutGenerationService.generateWorkout(user, request.workoutDuration);
            return toAiResponse(
                    fallback,
                    "AI tailoring unavailable. Used deterministic workout instead.",
                    "Gemini error: " + e.getMessage()
            );
        }
    }

    private AiWorkoutResponse toAiResponse(
            GeneratedWorkoutResponse deterministic,
            String summary,
            String warning
    ) {
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
