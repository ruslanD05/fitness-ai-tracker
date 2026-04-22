package com.ruslandontsov.fitness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruslandontsov.fitness.dto.AiWorkoutResponse;
import com.ruslandontsov.fitness.dto.ExerciseProgressionRecommendation;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import com.ruslandontsov.fitness.dto.WorkoutIntent;
import com.ruslandontsov.fitness.model.MuscleGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
public class GeminiService {

    private static final String MODEL =  "gemini-3-flash-preview";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public GeminiService(
            RestClient geminiRestClient,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key}") String apiKey
    ) {
        this.restClient = geminiRestClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
    }

    /**
     * Classifies the user's request into MUSCLE_CHANGE or TWEAK.
     * If MUSCLE_CHANGE, also extracts which muscle groups they want.
     * This call is intentionally minimal — just a few dozen tokens.
     */
    public WorkoutIntent extractIntent(String textRequest) {
        String availableGroups = String.join(", ",
                Arrays.stream(MuscleGroup.values())
                        .map(Enum::name)
                        .toList()
        );

        String prompt = """
                You classify a gym user's workout request.
                Available muscle groups: %s

                Rules:
                - If the user is asking to work different muscle groups than usual, return mode=MUSCLE_CHANGE and list the requested groups.
                - If the user is asking to adjust exercises, weight, sets, reps, intensity, or focus within a workout, return mode=TWEAK and empty requestedMuscleGroups.
                - When in doubt, return TWEAK.

                Return JSON only:
                {
                  "mode": "MUSCLE_CHANGE" or "TWEAK",
                  "requestedMuscleGroups": ["CHEST", "BACK", ...]
                }

                User request: %s
                """.formatted(availableGroups, textRequest);

        try {
            String json = callGemini(prompt);
            return objectMapper.readValue(json, WorkoutIntent.class);
        } catch (JsonProcessingException e) {
            return new WorkoutIntent(WorkoutIntent.Mode.TWEAK, List.of());
        }
    }

    /**
     * User wants different muscle groups. The deterministic system has already
     * generated a workout for those groups. Gemini assembles the final result
     * using the provided exercises and their progression data.
     */
    public AiWorkoutResponse assembleWorkout(
            GeneratedWorkoutResponse deterministicWorkout,
            Map<MuscleGroup, List<ExerciseProgressionRecommendation>> progressionCatalogue,
            String textRequest,
            int durationMinutes
    ) {
        try {
            String deterministicJson = objectMapper.writeValueAsString(deterministicWorkout);
            String catalogueJson = objectMapper.writeValueAsString(progressionCatalogue);

            String prompt = """
                    You are a fitness workout assistant.
                    The user wants to work specific muscle groups. The backend has generated a deterministic
                    workout proposal and provided the full exercise catalogue with progression data for those groups.

                    Your job:
                    - Use the deterministic proposal as your primary structure.
                    - You may swap or reorder exercises using others from the catalogue if it better matches the user's request.
                    - Only use exerciseTypeId values from the provided catalogue.
                    - Keep realistic sets/reps/rest/weight based on the progression data.
                    - Target duration: %d minutes.

                    Available exercises with progression data (grouped by muscle):
                    %s

                    Deterministic proposal:
                    %s

                    User request: %s

                    Return JSON only matching this schema:
                    {
                      "selectedMuscleGroups": ["string"],
                      "durationMinutes": integer,
                      "estimatedDurationSeconds": integer,
                      "aiSummary": "string",
                      "warning": "string",
                      "exercises": [
                        {
                          "exerciseTypeId": integer,
                          "exerciseName": "string",
                          "sets": integer,
                          "suggestedWeight": number,
                          "targetReps": integer,
                          "restSeconds": integer,
                          "progressionReason": "string"
                        }
                      ]
                    }
                    """.formatted(durationMinutes, catalogueJson, deterministicJson, textRequest);

            String json = callGemini(prompt);
            return objectMapper.readValue(json, AiWorkoutResponse.class);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse Gemini assemble response", e);
        }
    }

    /**
     * User wants to tweak the existing workout (exercises, weight, sets, etc.).
     * Gemini works freely within the already-generated workout.
     */
    public AiWorkoutResponse tailorWorkout(
            GeneratedWorkoutResponse deterministicWorkout,
            String textRequest
    ) {
        try {
            String deterministicJson = objectMapper.writeValueAsString(deterministicWorkout);

            String prompt = """
                    You are a fitness workout assistant.
                    The backend has already selected muscle groups and exercises.
                    Your job is ONLY to tailor the provided workout to better match the user's request.

                    Rules:
                    - Do NOT invent new exerciseTypeId values.
                    - Do NOT add exercises not present in the provided workout.
                    - Keep the same selectedMuscleGroups.
                    - Keep realistic sets/reps/rest/weight values.
                    - Keep the same overall duration target.

                    User request: %s

                    Workout to tailor:
                    %s

                    Return JSON only matching this schema:
                    {
                      "selectedMuscleGroups": ["string"],
                      "durationMinutes": integer,
                      "estimatedDurationSeconds": integer,
                      "aiSummary": "string",
                      "warning": "string",
                      "exercises": [
                        {
                          "exerciseTypeId": integer,
                          "exerciseName": "string",
                          "sets": integer,
                          "suggestedWeight": number,
                          "targetReps": integer,
                          "restSeconds": integer,
                          "progressionReason": "string"
                        }
                      ]
                    }
                    """.formatted(textRequest, deterministicJson);

            String json = callGemini(prompt);
            return objectMapper.readValue(json, AiWorkoutResponse.class);

        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse Gemini tailor response", e);
        }
    }


    private String callGemini(String prompt) {
        Map<String, Object> requestBody = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                ),
                "generationConfig", Map.of(
                        "response_mime_type", "application/json"
                )
        );

        GeminiResponse response = restClient.post()
                .uri("/v1beta/models/" + MODEL + ":generateContent?key=" + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(GeminiResponse.class);

        if (response == null
                || response.candidates() == null
                || response.candidates().isEmpty()) {
            throw new IllegalStateException("Empty response from Gemini");
        }

        GeminiCandidate candidate = response.candidates().get(0);
        if (candidate.content() == null
                || candidate.content().parts() == null
                || candidate.content().parts().isEmpty()) {
            throw new IllegalStateException("Gemini returned empty candidate content");
        }

        String text = candidate.content().parts().get(0).text();
        if (text == null || text.isBlank()) {
            throw new IllegalStateException("Gemini returned blank text");
        }

        text = text.strip();
        if (text.startsWith("```")) {
            text = text.replaceAll("^```[a-zA-Z]*\\n?", "").replaceAll("```$", "").strip();
        }

        return text;
    }

    public record GeminiResponse(List<GeminiCandidate> candidates) {}
    public record GeminiCandidate(GeminiContent content) {}
    public record GeminiContent(List<GeminiPart> parts) {}
    public record GeminiPart(String text) {}
}
