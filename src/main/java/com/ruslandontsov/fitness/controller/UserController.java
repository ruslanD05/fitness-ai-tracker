package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.dto.CreateWorkoutRequest;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.Workout;
import com.ruslandontsov.fitness.service.UserService;
import com.ruslandontsov.fitness.service.WorkoutService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final WorkoutService workoutService;

    public UserController(UserService userService, WorkoutService workoutService) {
         this.userService = userService;
         this.workoutService = workoutService;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        user.setId(null);
        return userService.createUser(user);
    }

    @GetMapping("/workouts/history")
    public List<Workout> getUserWorkouts() {
        return workoutService.getWorkoutsByUser(getCurrentUserId());
    }

    @PostMapping("/workouts")
    public Workout createWorkout(
            @RequestBody CreateWorkoutRequest request
    ) {
        Long userId = getCurrentUserId();
        return workoutService.createWorkout(userId, request);
    }

    private Long getCurrentUserId() {
        return 1L; // temporary fake login
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
}