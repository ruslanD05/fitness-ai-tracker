package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.model.Workout;
import com.ruslandontsov.fitness.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;

    public WorkoutService(WorkoutRepository workoutRepository) {
        this.workoutRepository = workoutRepository;
    }

    public Workout createWorkout(Workout workout) {
        return workoutRepository.save(workout);
    }

    public List<Workout> getWorkoutsByUser(Long userId) {
        return workoutRepository.findByUserId(userId);
    }
}