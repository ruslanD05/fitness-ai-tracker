package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.dto.CreateWorkoutRequest;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.Workout;
import com.ruslandontsov.fitness.service.SetEntryService;
import com.ruslandontsov.fitness.service.UserService;
import com.ruslandontsov.fitness.service.WorkoutService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;
    private final SetEntryService setEntryService;
    private final UserService userService;

    public WorkoutController(WorkoutService workoutService, SetEntryService setEntryService, UserService userService) {
        this.workoutService = workoutService;
        this.setEntryService = setEntryService;
        this.userService = userService;
    }

    @GetMapping("/{workoutId}/sets")
    public List<SetEntry> getWorkoutSets(@PathVariable Long workoutId) {
        if (!workoutService.existsById(workoutId)) {
            throw new RuntimeException("Workout not found");
        }
        return setEntryService.getSetsByWorkout(workoutId);
    }

    @GetMapping
    public List<Workout> getUserWorkouts() {
        return workoutService.getWorkoutsByUser(userService.getCurrentUserId());
    }

    @PostMapping
    public Workout createWorkout(
            @RequestBody CreateWorkoutRequest request
    ) {
        Long userId = userService.getCurrentUserId();
        return workoutService.createWorkout(userId, request);
    }
}