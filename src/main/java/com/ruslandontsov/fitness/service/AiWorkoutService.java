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
            // Call 1 — classify intent (cheap)
            WorkoutIntent intent = geminiService.extractIntent(request.textRequest);

            if (intent.mode() == WorkoutIntent.Mode.MUSCLE_CHANGE
                    && intent.requestedMuscleGroups() != null
                    && !intent.requestedMuscleGroups().isEmpty()) {

                // MUSCLE_CHANGE: run deterministic for the requested groups,
                // then let Gemini assemble the final workout from those exercises
                GeneratedWorkoutResponse deterministicForGroups =
                        workoutGenerationService.generateWorkout(
                                user,
                                request.workoutDuration,
                                intent.requestedMuscleGroups()
                        );

                Map<MuscleGroup, List<ExerciseProgressionRecommendation>> catalogue =
                        workoutGenerationService.loadCatalogueForGroups(user, intent.requestedMuscleGroups());

                // Call 2a — assemble using catalogue + deterministic proposal
                return geminiService.assembleWorkout(
                        deterministicForGroups,
                        catalogue,
                        request.textRequest,
                        request.workoutDuration
                );

            } else {
                // TWEAK: run deterministic normally, let Gemini adjust it
                GeneratedWorkoutResponse deterministic =
                        workoutGenerationService.generateWorkout(user, request.workoutDuration);

                // Call 2b — tailor the existing workout
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
