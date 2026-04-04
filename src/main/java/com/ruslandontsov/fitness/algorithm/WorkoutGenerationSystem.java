package com.ruslandontsov.fitness.algorithm;

import com.ruslandontsov.fitness.dto.*;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.TargetMuscle;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.UserGoal;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class WorkoutGenerationSystem {

    public GeneratedWorkoutResponse generateWorkout(
            User user,
            int durationMinutes,
            SuggestionResponseDto suggestionResponse,
            Map<MuscleGroup, List<ExerciseType>> exercisesByMuscle,
            Map<Long, ExerciseProgressionRecommendation> progressionByExerciseId
    ) {
        int remainingSeconds = durationMinutes * 60;

        List<MuscleGroup> selectedGroups = pickMuscleGroups(
                user,
                durationMinutes,
                suggestionResponse.suggestions()
        );

        List<GeneratedExerciseDto> chosenExercises = new ArrayList<>();
        Set<TargetMuscle> alreadyCovered = new HashSet<>();

        for (MuscleGroup group : selectedGroups) {
            List<ExerciseType> pool = exercisesByMuscle.getOrDefault(group, List.of());
            List<ExerciseType> ranked = rankExercisesByCoverage(pool, alreadyCovered);

            for (ExerciseType exercise : ranked) {
                ExerciseProgressionRecommendation progression =
                        progressionByExerciseId.get(exercise.getId());

                if (progression == null) continue;

                int sets = progression.isStable() ? 3 : 4;
                int restSeconds = progression.isStraightSets() ? 90 : 120;
                int estimatedExerciseSeconds =
                        sets * exercise.getAverageSetDurationSeconds() + (sets - 1) * restSeconds;

                if (estimatedExerciseSeconds > remainingSeconds) {
                    continue;
                }

                chosenExercises.add(new GeneratedExerciseDto(
                        exercise.getId(),
                        exercise.getName(),
                        sets,
                        progression.suggestedWeight(),
                        progression.targetReps(),
                        restSeconds,
                        progression.reason()
                ));

                if (exercise.getTargetMuscles() != null) {
                    alreadyCovered.addAll(exercise.getTargetMuscles());
                }

                remainingSeconds -= estimatedExerciseSeconds;

                if (chosenExercises.size() >= maxExercises(durationMinutes)) {
                    break;
                }
            }
        }

        int estimatedUsedSeconds = durationMinutes * 60 - remainingSeconds;

        return new GeneratedWorkoutResponse(
                selectedGroups,
                durationMinutes,
                estimatedUsedSeconds,
                chosenExercises
        );
    }

    private List<MuscleGroup> pickMuscleGroups(
            User user,
            int durationMinutes,
            List<MuscleSuggestionDto> suggestions
    ) {
        List<MuscleGroup> ordered = suggestions.stream()
                .map(MuscleSuggestionDto::muscleGroup)
                .toList();

        List<MuscleGroup> upperBody = List.of(
                MuscleGroup.CHEST, MuscleGroup.BACK, MuscleGroup.SHOULDERS, MuscleGroup.ARMS
        );

        List<MuscleGroup> preferredOrder = new ArrayList<>();

        if (user.getGoal() == UserGoal.WEIGHT_LOSS) {
            if (ordered.contains(MuscleGroup.LEGS)) preferredOrder.add(MuscleGroup.LEGS);
            ordered.stream()
                    .filter(g -> g != MuscleGroup.LEGS)
                    .forEach(preferredOrder::add);
        } else {
            ordered.stream()
                    .filter(upperBody::contains)
                    .forEach(preferredOrder::add);

            ordered.stream()
                    .filter(g -> !upperBody.contains(g))
                    .forEach(preferredOrder::add);
        }

        int muscleCount = durationMinutes >= 50 ? 2 : 1;

        return preferredOrder.stream()
                .distinct()
                .limit(muscleCount)
                .toList();
    }

    private List<ExerciseType> rankExercisesByCoverage(
            List<ExerciseType> exercises,
            Set<TargetMuscle> alreadyCovered
    ) {
        return exercises.stream()
                .sorted(Comparator
                        .comparingInt((ExerciseType e) -> uncoveredCount(e, alreadyCovered)).reversed()
                        .thenComparing(ExerciseType::getAverageSetDurationSeconds))
                .collect(Collectors.toList());
    }

    private int uncoveredCount(ExerciseType exercise, Set<TargetMuscle> alreadyCovered) {
        if (exercise.getTargetMuscles() == null) return 0;

        return (int) exercise.getTargetMuscles().stream()
                .filter(tm -> !alreadyCovered.contains(tm))
                .count();
    }

    private int maxExercises(int durationMinutes) {
        if (durationMinutes <= 30) return 2;
        if (durationMinutes <= 45) return 4;
        return 6;
    }
}
