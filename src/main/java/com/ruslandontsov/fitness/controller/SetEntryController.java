package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.dto.CreateSetEntryRequest;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.service.SetEntryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sets")
public class SetEntryController {

    private final SetEntryService setService;

    public SetEntryController(SetEntryService setService) {
        this.setService = setService;
    }

    @PostMapping("/{workoutId}/sets")
    public SetEntry createSet(
            @PathVariable Long workoutId,
            @Valid @RequestBody CreateSetEntryRequest request
    ) {
        return setService.createSet(workoutId, request);
    }

    @PatchMapping("/{setId}/complete")
    public ResponseEntity<?> completeSet(@PathVariable Long setId) {
        setService.completeSet(setId);
        return ResponseEntity.ok(Map.of("message", "Set marked as complete"));
    }
}