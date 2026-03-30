package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.dto.RecoveryResponseDto;
import com.ruslandontsov.fitness.dto.SuggestionResponseDto;
import com.ruslandontsov.fitness.exception.ResourceNotFoundException;
import com.ruslandontsov.fitness.model.MuscleRecovery;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.repository.MuscleRecoveryRepository;
import com.ruslandontsov.fitness.service.MuscleRecoveryService;
import com.ruslandontsov.fitness.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/recovery")
public class MuscleRecoveryController {

    private final MuscleRecoveryService recoveryService;
    private final UserService userService;

    public MuscleRecoveryController(MuscleRecoveryService recoveryService, UserService userService) {
        this.recoveryService = recoveryService;
        this.userService = userService;
    }

    @GetMapping
    public List<RecoveryResponseDto> getRecoveryMap() {
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return recoveryService.getRecoveryMapForUser(user);

    }

    @GetMapping("/suggest")
    public SuggestionResponseDto suggestNextMuscle() {
        Long userId = userService.getCurrentUserId();
        User user = userService.getUserById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return recoveryService.getSuggestionForUser(user);
    }
}