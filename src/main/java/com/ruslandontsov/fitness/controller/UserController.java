package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.UserExperience;
import com.ruslandontsov.fitness.model.UserGoal;
import com.ruslandontsov.fitness.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
         this.userService = userService;
    }

    @PostMapping("/me/exp")
    public void setExperienceLevel(@RequestBody UserExperience userExperience) {
        userService.updateUserExperience(userExperience);
    }

    @PostMapping("/me/goal")
    public void setExperienceLevel(@RequestBody UserGoal userGoal) {
        userService.updateUserGoal(userGoal);
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getAllUsers();
    }
}