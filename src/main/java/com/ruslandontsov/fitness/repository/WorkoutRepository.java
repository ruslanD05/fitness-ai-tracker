package com.ruslandontsov.fitness.repository;

import com.ruslandontsov.fitness.model.Workout;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkoutRepository extends JpaRepository<Workout, Long> {
    List<Workout> findByUserId(Long userId);
    Optional<Workout> findByIdAndUserId(Long workoutId, Long userId);
}