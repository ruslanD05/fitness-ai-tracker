package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.dto.GenerateWorkoutRequest;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import com.ruslandontsov.fitness.exception.ResourceNotFoundException;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.service.UserService;
import com.ruslandontsov.fitness.service.WorkoutGenerationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/workout-generation")
public class WorkoutGenerationController {

    private final WorkoutGenerationService workoutGenerationService;
    private final UserService userService;

    public WorkoutGenerationController(
            WorkoutGenerationService workoutGenerationService,
            UserService userService
    ) {
        this.workoutGenerationService = workoutGenerationService;
        this.userService = userService;
    }

    @PostMapping("/generate")
    public GeneratedWorkoutResponse generate(@RequestBody GenerateWorkoutRequest request) {
        Long userId = userService.getCurrentUserId();

        User user = userService.getUserById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return workoutGenerationService.generateWorkout(user, request.workoutDuration);
    }
}