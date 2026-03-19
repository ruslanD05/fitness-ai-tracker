package com.ruslandontsov.fitness.repository;

import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExerciseTypeRepository extends JpaRepository<ExerciseType, Long> {
    Optional<ExerciseType> findByName(String exerciseName);
    List<ExerciseType> findByMuscleGroup(MuscleGroup muscleGroup);
}