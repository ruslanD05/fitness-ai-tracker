package com.ruslandontsov.fitness.algorithm;

import com.ruslandontsov.fitness.dto.SuggestionResponseDto;
import com.ruslandontsov.fitness.dto.WorkoutGeneratorResponse;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.service.MuscleRecoveryService;
import com.ruslandontsov.fitness.service.UserService;

public class WorkoutGenerationSystem {
    private final MuscleRecoveryService muscleRecoveryService;

    public WorkoutGenerationSystem(MuscleRecoveryService muscleRecoveryService, UserService userService) {
        this.muscleRecoveryService = muscleRecoveryService;
    }

    public WorkoutGeneratorResponse generateWorkout(User user, int workoutDuration) {
        SuggestionResponseDto recoverySuggestion = muscleRecoveryService.getSuggestionForUser(user);

    }
}
