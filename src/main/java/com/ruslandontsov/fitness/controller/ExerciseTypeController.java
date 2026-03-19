package com.ruslandontsov.fitness.controller;


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

    @GetMapping("/search")
    public ExerciseType getByName(@RequestParam String exerciseName){
        return exerciseTypeService.getExerciseTypeByName(exerciseName).orElseThrow(() -> new RuntimeException("Exercise not found"));
    }

    @GetMapping("/{id}")
    public ExerciseType getById(@PathVariable Long id) {
        return exerciseTypeService.getExerciseTypeById(id).orElseThrow(() -> new RuntimeException("Exercise not found"));
    }
    
    @GetMapping("/{muscle_group}")
    public List<ExerciseType> getByMuscleGroup(@PathVariable MuscleGroup muscle_group){
        return exerciseTypeService.getExerciseTypeByMuscleGroup(muscle_group);
    }

    @GetMapping
    public List<ExerciseType> getExerciseTypes() {
        return exerciseTypeService.getExerciseTypes();
    }
}