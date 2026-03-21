package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.dto.LoginRequest;
import com.ruslandontsov.fitness.dto.RegisterRequest;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if(userService.existsByEmail(registerRequest.email)) {
            return ResponseEntity.badRequest().body("Email already in use");
        }
        User user = userService.register(registerRequest.name, registerRequest.email, registerRequest.password);
        return ResponseEntity.ok(Map.of("message", "Registered successfully", "userId", user.getId()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String token = userService.login(loginRequest.email, loginRequest.password);
        return ResponseEntity.ok(Map.of("token", token));
    }
}
