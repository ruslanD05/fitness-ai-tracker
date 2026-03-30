package com.ruslandontsov.fitness.controller;


import com.ruslandontsov.fitness.exception.ResourceNotFoundException;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.service.ExerciseTypeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exercises")
public class ExerciseTypeController {

    private final ExerciseTypeService exerciseTypeService;

    public ExerciseTypeController(ExerciseTypeService exerciseTypeService) {
        this.exerciseTypeService = exerciseTypeService;
    }

    @PostMapping
    public ExerciseType createExerciseType(@RequestBody ExerciseType exercise) {
        exercise.setId(null);
        return exerciseTypeService.createExercise(exercise);
    }

    @PostMapping("/bulk")
    public List<ExerciseType> createBulk(@RequestBody List<ExerciseType> exercises) {
        return exerciseTypeService.createBulk(exercises);
    }

    @GetMapping("/search")
    public ExerciseType getByName(@RequestParam String exerciseName){
        return exerciseTypeService.getExerciseTypeByName(exerciseName).orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
    }

    @GetMapping("/id/{id}")
    public ExerciseType getById(@PathVariable Long id) {
        return exerciseTypeService.getExerciseTypeById(id).orElseThrow(() -> new ResourceNotFoundException("Exercise not found"));
    }

    @GetMapping("/muscle-group/{muscleGroup}")
    public List<ExerciseType> getByMuscleGroup(@PathVariable MuscleGroup muscleGroup){
        return exerciseTypeService.getExerciseTypeByMuscleGroup(muscleGroup);
    }

    @GetMapping
    public List<ExerciseType> getExerciseTypes() {
        return exerciseTypeService.getExerciseTypes();
    }
}