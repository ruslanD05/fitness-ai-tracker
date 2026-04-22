package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.ExerciseProgressionRecommendation;
import com.ruslandontsov.fitness.algorithm.ExerciseProgressionSystem;
import com.ruslandontsov.fitness.algorithm.WorkoutGenerationSystem;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import com.ruslandontsov.fitness.dto.MuscleSuggestionDto;
import com.ruslandontsov.fitness.dto.SuggestionResponseDto;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.repository.ExerciseTypeRepository;
import com.ruslandontsov.fitness.repository.SetEntryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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



    /**
     * Loads exercises and progression recommendations only for the specified muscle groups.
     * Used to build the catalogue passed to Gemini for MUSCLE_CHANGE requests.
     */
    public Map<MuscleGroup, List<ExerciseProgressionRecommendation>> loadCatalogueForGroups(
            User user,
            List<MuscleGroup> groups
    ) {
        Map<MuscleGroup, List<ExerciseProgressionRecommendation>> catalogue = new HashMap<>();

        for (MuscleGroup group : groups) {
            List<ExerciseType> exercises = exerciseTypeRepository.findByPrimaryMuscleGroup(group);
            List<ExerciseProgressionRecommendation> recommendations = new ArrayList<>();

            for (ExerciseType exercise : exercises) {
                List<SetEntry> recentSets = setEntryRepository.findRecentCompletedSetsByUserAndExerciseType(
                        user.getId(),
                        exercise.getId(),
                        PageRequest.of(0, exerciseProgressionSystem.getLookbackSets())
                );
                recommendations.add(exerciseProgressionSystem.recommend(exercise, recentSets));
            }

            if (!recommendations.isEmpty()) {
                catalogue.put(group, recommendations);
            }
        }

        return catalogue;
    }

    /**
     * Loads every exercise in the database and computes a progression recommendation
     * for the given user against each one. Used to feed the full catalogue to Gemini.
     */
    public Map<MuscleGroup, List<ExerciseProgressionRecommendation>> loadFullCatalogue(User user) {
        Map<MuscleGroup, List<ExerciseProgressionRecommendation>> catalogue = new HashMap<>();

        for (MuscleGroup group : MuscleGroup.values()) {
            List<ExerciseType> exercises = exerciseTypeRepository.findByPrimaryMuscleGroup(group);
            List<ExerciseProgressionRecommendation> recommendations = new ArrayList<>();

            for (ExerciseType exercise : exercises) {
                List<SetEntry> recentSets = setEntryRepository.findRecentCompletedSetsByUserAndExerciseType(
                        user.getId(),
                        exercise.getId(),
                        PageRequest.of(0, exerciseProgressionSystem.getLookbackSets())
                );
                recommendations.add(exerciseProgressionSystem.recommend(exercise, recentSets));
            }

            if (!recommendations.isEmpty()) {
                catalogue.put(group, recommendations);
            }
        }

        return catalogue;
    }

    public GeneratedWorkoutResponse generateWorkout(User user, int durationMinutes) {
        return generateWorkout(user, durationMinutes, null);
    }

    public GeneratedWorkoutResponse generateWorkout(
            User user,
            int durationMinutes,
            List<MuscleGroup> preferredMuscleGroups
    ) {
        Map<MuscleGroup, List<ExerciseType>> exercisesByMuscle = new HashMap<>();
        Map<Long, ExerciseProgressionRecommendation> progressionByExerciseId = new HashMap<>();

        SuggestionResponseDto suggestions;

        if (preferredMuscleGroups == null || preferredMuscleGroups.isEmpty()) {
            suggestions = muscleRecoveryService.getSuggestionForUser(user);
        } else {
            List<MuscleSuggestionDto> preferredSuggestions = preferredMuscleGroups.stream()
                    .map(group -> new MuscleSuggestionDto(
                            group,
                            "User explicitly requested this muscle group"
                    ))
                    .toList();

            suggestions = new SuggestionResponseDto(preferredSuggestions);
        }

        for (MuscleSuggestionDto suggestion : suggestions.suggestions()) {
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