package com.ruslandontsov.fitness.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruslandontsov.fitness.dto.AiWorkoutIntent;
import com.ruslandontsov.fitness.dto.AiWorkoutResponse;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class OpenRouterService {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public OpenRouterService(
            RestClient openRouterRestClient,
            ObjectMapper objectMapper,
            @Value("${openrouter.api.key}") String apiKey,
            @Value("${openrouter.model}") String model
    ) {
        this.restClient = openRouterRestClient;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public AiWorkoutIntent parseIntent(String textRequest) {
        if (textRequest == null || textRequest.isBlank()) {
            return new AiWorkoutIntent(
                    List.of(),
                    false,
                    "No explicit muscle preference provided."
            );
        }

        String systemPrompt = """
                You extract workout intent from a user's request.
                Return only valid JSON matching the schema.
                You may only use these primary muscle groups:
                CHEST, BACK, LEGS, SHOULDERS, ARMS, CORE.
                If the request does not clearly ask for a muscle group, return an empty list.
                forcePreference should be true only if the user explicitly requests a muscle group.
                """;

        String userPrompt = """
                User request:
                %s
                """.formatted(textRequest);

        String json = requestStructuredResponse(
                "ai_workout_intent",
                buildIntentSchema(),
                systemPrompt,
                userPrompt
        );

        try {
            return objectMapper.readValue(json, AiWorkoutIntent.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI intent response", e);
        }
    }

    public AiWorkoutResponse tailorWorkout(
            GeneratedWorkoutResponse deterministicWorkout,
            String textRequest
    ) {
        try {
            String deterministicJson = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(deterministicWorkout);

            String systemPrompt = """
                    You are a fitness workout assistant.
                    The backend has already selected the muscle groups and allowed exercises.
                    Your job is only to tailor the provided workout to better match the user's request.
                    Do not invent new exercise ids.
                    Do not add exercises not present in the provided workout.
                    Keep the same selectedMuscleGroups.
                    Return only valid JSON matching the schema.
                    """;

            String userPrompt = """
                    User request:
                    %s

                    Deterministic workout proposal:
                    %s

                    Tailor the workout while preserving:
                    - same exerciseTypeId values
                    - realistic sets/reps/rest/weight
                    - same overall duration target
                    """.formatted(
                    textRequest == null || textRequest.isBlank() ? "No extra request" : textRequest,
                    deterministicJson
            );

            String json = requestStructuredResponse(
                    "ai_workout_response",
                    buildWorkoutSchema(),
                    systemPrompt,
                    userPrompt
            );

            return objectMapper.readValue(json, AiWorkoutResponse.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to parse AI workout response", e);
        }
    }

    private String requestStructuredResponse(
            String schemaName,
            Map<String, Object> schema,
            String systemPrompt,
            String userPrompt
    ) {
        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                ),
                "response_format", Map.of(
                        "type", "json_schema",
                        "json_schema", Map.of(
                                "name", schemaName,
                                "strict", true,
                                "schema", schema
                        )
                )
        );

        OpenRouterResponse response = restClient.post()
                .uri("/chat/completions")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(requestBody)
                .retrieve()
                .body(OpenRouterResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("Empty response from OpenRouter");
        }

        Message message = response.choices().get(0).message();
        if (message == null || message.content() == null || message.content().isBlank()) {
            throw new IllegalStateException("OpenRouter returned empty message content");
        }

        return message.content();
    }

    private Map<String, Object> buildIntentSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "preferredMuscleGroups", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "forcePreference", Map.of("type", "boolean"),
                        "summary", Map.of("type", "string")
                ),
                "required", List.of("preferredMuscleGroups", "forcePreference", "summary")
        );
    }

    private Map<String, Object> buildWorkoutSchema() {
        return Map.of(
                "type", "object",
                "additionalProperties", false,
                "properties", Map.of(
                        "selectedMuscleGroups", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "durationMinutes", Map.of("type", "integer"),
                        "estimatedDurationSeconds", Map.of("type", "integer"),
                        "aiSummary", Map.of("type", "string"),
                        "exercises", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "properties", Map.of(
                                                "exerciseTypeId", Map.of("type", "integer"),
                                                "exerciseName", Map.of("type", "string"),
                                                "sets", Map.of("type", "integer"),
                                                "suggestedWeight", Map.of("type", "number"),
                                                "targetReps", Map.of("type", "integer"),
                                                "restSeconds", Map.of("type", "integer"),
                                                "progressionReason", Map.of("type", "string")
                                        ),
                                        "required", List.of(
                                                "exerciseTypeId",
                                                "exerciseName",
                                                "sets",
                                                "suggestedWeight",
                                                "targetReps",
                                                "restSeconds",
                                                "progressionReason"
                                        )
                                )
                        )
                ),
                "required", List.of(
                        "selectedMuscleGroups",
                        "durationMinutes",
                        "estimatedDurationSeconds",
                        "exercises",
                        "aiSummary"
                )
        );
    }

    public record OpenRouterResponse(List<Choice> choices) {}
    public record Choice(Message message) {}
    public record Message(String content) {}
}