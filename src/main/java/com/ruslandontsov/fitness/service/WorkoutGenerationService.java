package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.ExerciseProgressionRecommendation;
import com.ruslandontsov.fitness.algorithm.ExerciseProgressionSystem;
import com.ruslandontsov.fitness.algorithm.WorkoutGenerationSystem;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import com.ruslandontsov.fitness.dto.SuggestionResponseDto;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.repository.ExerciseTypeRepository;
import com.ruslandontsov.fitness.repository.SetEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class WorkoutGenerationService {

    private final MuscleRecoveryService muscleRecoveryService;
    private final ExerciseTypeRepository exerciseTypeRepository;
    private final SetEntryRepository setEntryRepository;
    private final ExerciseProgressionSystem exerciseProgressionSystem;
    private final WorkoutGenerationSystem workoutGenerationSystem;

    public WorkoutGenerationService(
            MuscleRecoveryService muscleRecoveryService,
            ExerciseTypeRepository exerciseTypeRepository,
            SetEntryRepository setEntryRepository,
            ExerciseProgressionSystem exerciseProgressionSystem,
            WorkoutGenerationSystem workoutGenerationSystem
    ) {
        this.muscleRecoveryService = muscleRecoveryService;
        this.exerciseTypeRepository = exerciseTypeRepository;
        this.setEntryRepository = setEntryRepository;
        this.exerciseProgressionSystem = exerciseProgressionSystem;
        this.workoutGenerationSystem = workoutGenerationSystem;
    }

    public GeneratedWorkoutResponse generateWorkout(User user, int durationMinutes) {
        SuggestionResponseDto suggestions = muscleRecoveryService.getSuggestionForUser(user);

        Map<MuscleGroup, List<ExerciseType>> exercisesByMuscle = new HashMap<>();
        Map<Long, ExerciseProgressionRecommendation> progressionByExerciseId = new HashMap<>();

        for (var suggestion : suggestions.suggestions()) {
            MuscleGroup group = suggestion.muscleGroup();

            List<ExerciseType> exercises = exerciseTypeRepository.findByPrimaryMuscleGroup(group);
            exercisesByMuscle.put(group, exercises);

            for (ExerciseType exercise : exercises) {
                List<SetEntry> recentSets = setEntryRepository.findRecentCompletedSetsByUserAndExerciseType(
                        user.getId(),
                        exercise.getId(),
                        PageRequest.of(0, exerciseProgressionSystem.getLookbackSets())
                );

                ExerciseProgressionRecommendation recommendation =
                        exerciseProgressionSystem.recommend(exercise, recentSets);

                progressionByExerciseId.put(exercise.getId(), recommendation);
            }
        }

        return workoutGenerationSystem.generateWorkout(
                user,
                durationMinutes,
                suggestions,
                exercisesByMuscle,
                progressionByExerciseId
        );
    }
}