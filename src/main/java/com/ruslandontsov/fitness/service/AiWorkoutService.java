package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.AiWorkoutResponse;
import com.ruslandontsov.fitness.dto.GenerateWorkoutRequest;
import netscape.javascript.JSObject;
import org.springframework.stereotype.Service;

@Service
public class AiWorkoutService {
    private ExerciseTypeService exerciseTypeService;

    public AiWorkoutService(ExerciseTypeService exerciseTypeService){
        this.exerciseTypeService = exerciseTypeService;
    }

    public AiWorkoutResponse generateWorkout(GenerateWorkoutRequest request){

    }
}
