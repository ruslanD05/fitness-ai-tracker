package com.ruslandontsov.fitness.controller;

import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.service.SetEntryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sets")
public class SetEntryController {

    private final SetEntryService setService;

    public SetEntryController(SetEntryService setService) {
        this.setService = setService;
    }

    @PostMapping
    public SetEntry createSet(@RequestBody SetEntry set) {
        return setService.createSet(set);
    }

    @GetMapping("/workout/{workoutId}")
    public List<SetEntry> getWorkoutSets(@PathVariable Long workoutId) {
        return setService.getSetsByWorkout(workoutId);
    }
}