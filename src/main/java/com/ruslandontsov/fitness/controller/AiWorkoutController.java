package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.dto.AiWorkoutResponse;
import com.ruslandontsov.fitness.dto.GenerateWorkoutRequest;
import com.ruslandontsov.fitness.service.AiWorkoutService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiWorkoutController {
    private final AiWorkoutService aiWorkoutService;

    public AiWorkoutController(AiWorkoutService aiWorkoutService){
        this.aiWorkoutService = aiWorkoutService;
    }

    @PostMapping("/generate-workout")
    public AiWorkoutResponse build_workout(@Valid @RequestBody GenerateWorkoutRequest request){
        return aiWorkoutService.generateWorkout(request);
    }
}
