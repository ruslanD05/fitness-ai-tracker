package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.model.Workout;
import com.ruslandontsov.fitness.service.WorkoutService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService workoutService;

    public WorkoutController(WorkoutService workoutService) {
        this.workoutService = workoutService;
    }

    @PostMapping
    public Workout createWorkout(@RequestBody Workout workout) {
        return workoutService.createWorkout(workout);
    }

    @GetMapping("/user/{userId}")
    public List<Workout> getUserWorkouts(@PathVariable Long userId) {
        return workoutService.getWorkoutsByUser(userId);
    }
}