package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.CreateSetEntryRequest;
import com.ruslandontsov.fitness.dto.CreateWorkoutRequest;
import com.ruslandontsov.fitness.dto.GeneratedExerciseDto;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import com.ruslandontsov.fitness.exception.ResourceNotFoundException;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.Workout;
import com.ruslandontsov.fitness.repository.SetEntryRepository;
import com.ruslandontsov.fitness.repository.UserRepository;
import com.ruslandontsov.fitness.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final UserRepository userRepository;
    private final SetEntryService setEntryService;
    private final SetEntryRepository setEntryRepository;

    public WorkoutService(WorkoutRepository workoutRepository, UserRepository userRepository, SetEntryService setEntryService, SetEntryRepository setEntryRepository) {
        this.workoutRepository = workoutRepository;
        this.userRepository = userRepository;
        this.setEntryService = setEntryService;
        this.setEntryRepository = setEntryRepository;
    }

    public Workout createWorkout(Long userId, CreateWorkoutRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setName(request.name);
        workout.setDate(LocalDate.now());

        return workoutRepository.save(workout);
    }

    public Workout createGeneratedWorkout(Long userId, GeneratedWorkoutResponse request) {
        User user = userRepository.findById(userId).orElseThrow();

        Workout workout = new Workout();
        workout.setUser(user);
        workout.setDate(LocalDate.now());
        Workout workoutS = workoutRepository.save(workout);

        workoutS.setName("Generated_workout_" + workoutS.getId());

        for (GeneratedExerciseDto exercise : request.exercises()){
            for (int set = 0;set < exercise.sets();set++) {
                CreateSetEntryRequest createSetEntryRequest = new CreateSetEntryRequest();
                createSetEntryRequest.exerciseTypeId = exercise.exerciseTypeId();
                createSetEntryRequest.weight = exercise.suggestedWeight();
                createSetEntryRequest.reps = exercise.targetReps();
                createSetEntryRequest.rest = exercise.restSeconds();
                setEntryService.createSet(workoutS.getId(), createSetEntryRequest);
            }
        }

        return workoutS;
    }

    public void deleteWorkout(Long userId, Long workoutId) {
        Workout workout = workoutRepository.findByIdAndUserId(workoutId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Workout not found"));

        List<SetEntry> setsOfWorkout = setEntryRepository.findByWorkoutId(workoutId);
        setEntryRepository.deleteAll(setsOfWorkout);
        workoutRepository.delete(workout);
    }

    public List<Workout> getWorkoutsByUser(Long userId) {
        return workoutRepository.findByUserId(userId);
    }

    public boolean existsById(long id) {return workoutRepository.existsById(id);}
}