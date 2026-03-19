package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.CreateWorkoutRequest;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.Workout;
import com.ruslandontsov.fitness.repository.UserRepository;
import com.ruslandontsov.fitness.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;

    public WorkoutService(WorkoutRepository workoutRepository, UserRepository userRepository) {

        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
    }

    public Workout createWorkout(Long userId, CreateWorkoutRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setName(request.name);
        workout.setDate(request.date);

        return workoutRepository.save(workout);
    }

    public List<Workout> getWorkoutsByUser(Long userId) {
        return workoutRepository.findByUserId(userId);
    }

    public boolean existsById(long id) {return workoutRepository.existsById(id);}
}