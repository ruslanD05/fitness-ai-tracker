package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.repository.ExerciseTypeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
public class ExerciseTypeService {

    private final ExerciseTypeRepository exerciseTypeRepository;

    public ExerciseTypeService(ExerciseTypeRepository exerciseTypeRepository) {
        this.exerciseTypeRepository = exerciseTypeRepository;
    }

    public ExerciseType createExercise(ExerciseType exercise) {
        return exerciseTypeRepository.save(exercise);
    }

    public Optional<ExerciseType> getExerciseTypeByName(String exerciseName) {
        return exerciseTypeRepository.findByName(exerciseName);
    }

    public Optional<ExerciseType> getExerciseTypeById(Long id) {return exerciseTypeRepository.findById(id);}

    public List<ExerciseType> getExerciseTypes() {
        return exerciseTypeRepository.findAll();
    }

    public List<ExerciseType> getExerciseTypeByMuscleGroup(MuscleGroup muscleGroup) {return exerciseTypeRepository.findByMuscleGroup(muscleGroup);}
}
