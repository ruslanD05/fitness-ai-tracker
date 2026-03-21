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

    public UserController(UserService userService, WorkoutService workoutService) {
         this.userService = userService;
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        user.setId(null);
        return userService.createUser(user);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
}