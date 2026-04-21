package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.dto.AiWorkoutResponse;
import com.ruslandontsov.fitness.dto.GenerateWorkoutRequest;
import com.ruslandontsov.fitness.exception.ResourceNotFoundException;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.service.AiWorkoutService;
import com.ruslandontsov.fitness.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiWorkoutController {

    private final AiWorkoutService aiWorkoutService;
    private final UserService userService;

    public AiWorkoutController(AiWorkoutService aiWorkoutService, UserService userService) {
        this.aiWorkoutService = aiWorkoutService;
        this.userService = userService;
    }

    @PostMapping("/generate-workout")
    public AiWorkoutResponse generateWorkout(@Valid @RequestBody GenerateWorkoutRequest request) {
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return aiWorkoutService.generateWorkout(user, request);
    }
}
