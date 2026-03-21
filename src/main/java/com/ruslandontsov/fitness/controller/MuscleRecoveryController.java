package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.model.MuscleRecovery;
import com.ruslandontsov.fitness.repository.MuscleRecoveryRepository;
import com.ruslandontsov.fitness.security.MuscleRecoveryService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recovery")
public class MuscleRecoveryController {

    private final MuscleRecoveryService recoveryService;
    private final MuscleRecoveryRepository recoveryRepository;

    public MuscleRecoveryController(MuscleRecoveryService recoveryService, MuscleRecoveryRepository recoveryRepository) {
        this.recoveryService = recoveryService;
        this.recoveryRepository = recoveryRepository;
    }

    private Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    @GetMapping
    public List<Map<String, Object>> getRecoveryMap() {
        Long userId = getCurrentUserId();
        List<MuscleRecovery> records = recoveryRepository.findByUserId(userId);

        return records.stream()
                .map(r -> Map.of(
                        "muscle", (Object) r.getMuscleGroup().name(),
                        "status", recoveryService.computeStatus(r),
                        "fatigue", Math.round(recoveryService.getCurrentFatigue(r))
                ))
                .toList();
    }

    @GetMapping("/suggest")
    public Map<String, String> suggestNextMuscle() {
        Long userId = getCurrentUserId();
        List<MuscleRecovery> records = recoveryRepository.findByUserId(userId);

        MuscleRecovery best = null;
        double lowestFatigue = Double.MAX_VALUE;

        for (MuscleRecovery r : records) {
            double fatigue = recoveryService.getCurrentFatigue(r);
            if (fatigue >= 60) continue; // sore, skip entirely
            if (fatigue < lowestFatigue) {
                lowestFatigue = fatigue;
                best = r;
            }
        }

        if (best == null) {
            return Map.of(
                    "suggested", "REST",
                    "reason", "All muscle groups are still fatigued — take a rest day"
            );
        }

        String reason = lowestFatigue == 0.0
                ? "Never trained"
                : "Fatigue score " + Math.round(lowestFatigue) + " — most recovered";

        return Map.of(
                "suggested", best.getMuscleGroup().name(),
                "reason", reason
        );
    }
}