package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.CreateSetEntryRequest;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.Workout;
import com.ruslandontsov.fitness.repository.ExerciseTypeRepository;
import com.ruslandontsov.fitness.repository.SetEntryRepository;
import com.ruslandontsov.fitness.repository.WorkoutRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SetEntryService {

    private final SetEntryRepository setRepository;
    private final WorkoutRepository workoutRepository;
    private final ExerciseTypeRepository exerciseTypeRepository;

    public SetEntryService(SetEntryRepository setRepository, WorkoutRepository workoutRepository, ExerciseTypeRepository exerciseTypeRepository) {
        this.setRepository = setRepository;
        this.workoutRepository = workoutRepository;
        this.exerciseTypeRepository = exerciseTypeRepository;
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
}