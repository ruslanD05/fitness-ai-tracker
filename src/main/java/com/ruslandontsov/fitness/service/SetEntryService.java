package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.CreateSetEntryRequest;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.Workout;
import com.ruslandontsov.fitness.repository.ExerciseTypeRepository;
import com.ruslandontsov.fitness.repository.SetEntryRepository;
import com.ruslandontsov.fitness.repository.WorkoutRepository;
import com.ruslandontsov.fitness.security.MuscleRecoveryService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class SetEntryService {

    private final SetEntryRepository setRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final MuscleRecoveryService muscleRecoveryService;

    public SetEntryService(SetEntryRepository setRepository, WorkoutRepository workoutRepository, ExerciseTypeRepository exerciseTypeRepository,  MuscleRecoveryService muscleRecoveryService) {
        this.setRepository = setRepository;
        this.workoutRepository = workoutRepository;
        this.exerciseTypeRepository = exerciseTypeRepository;
        this.muscleRecoveryService = muscleRecoveryService;
    }

    public List<SetEntry> getSetsByWorkout(Long workoutId) {
        return setRepository.findByWorkoutId(workoutId);
    }

    public SetEntry createSet(CreateSetEntryRequest request) {

        ExerciseType exercise = exerciseTypeRepository
                .findById(request.exerciseTypeId)
                .orElseThrow(() -> new RuntimeException("Exercise not found"));

        Workout workout = workoutRepository
                .findById(request.workoutId)
                .orElseThrow(() -> new RuntimeException("Workout not found"));

        SetEntry set = new SetEntry();
        set.setReps(request.reps);
        set.setWeight(request.weight);
        set.setExerciseType(exercise);
        set.setWorkout(workout);

        return setRepository.save(set);
    }
    public void completeSet(Long setId) {
        SetEntry set = setRepository.findById(setId).orElseThrow(() -> new RuntimeException("Set not found"));
        if (set.isCompleted()) return; // already completed, do nothing

        set.setCompleted(true);
        setRepository.save(set);

        muscleRecoveryService.recoveryUpdate(
                set.getWorkout().getUser(),
                set
        );
    }
}