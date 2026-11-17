package com.ruslandontsov.fitness.algorithm;

import com.ruslandontsov.fitness.dto.ExerciseProgressionRecommendation;
import com.ruslandontsov.fitness.dto.GeneratedExerciseDto;
import com.ruslandontsov.fitness.dto.GeneratedWorkoutResponse;
import com.ruslandontsov.fitness.dto.MuscleSuggestionDto;
import com.ruslandontsov.fitness.dto.SuggestionResponseDto;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.TargetMuscle;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.UserGoal;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class WorkoutGenerationSystemTest {

    private final WorkoutGenerationSystem system = new WorkoutGenerationSystem();

    private final ExerciseType inclinePress = exercise(1L, 40, TargetMuscle.UPPER_CHEST, TargetMuscle.MID_CHEST);
    private final ExerciseType flyes = exercise(2L, 30, TargetMuscle.MID_CHEST);
    private final ExerciseType dips = exercise(3L, 30, TargetMuscle.LOWER_CHEST);

    private final Map<MuscleGroup, List<ExerciseType>> chestPool =
            Map.of(MuscleGroup.CHEST, List.of(flyes, dips, inclinePress));

    private final Map<Long, ExerciseProgressionRecommendation> progression = Map.of(
            1L, stable(1L),
            2L, stable(2L),
            3L, stable(3L)
    );

    @Test
    void muscleGrowthPrefersUpperBody() {
        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.MUSCLE_GROWTH), 60,
                suggestions(MuscleGroup.LEGS, MuscleGroup.CORE, MuscleGroup.BACK, MuscleGroup.CHEST),
                Map.of(), Map.of());

        assertThat(workout.selectedMuscleGroups()).containsExactly(MuscleGroup.BACK, MuscleGroup.CHEST);
    }

    @Test
    void weightLossPutsLegsFirst() {
        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.WEIGHT_LOSS), 60,
                suggestions(MuscleGroup.CHEST, MuscleGroup.LEGS, MuscleGroup.BACK),
                Map.of(), Map.of());

        assertThat(workout.selectedMuscleGroups()).containsExactly(MuscleGroup.LEGS, MuscleGroup.CHEST);
    }

    @Test
    void shortWorkoutsTrainOneMuscleGroup() {
        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.MUSCLE_GROWTH), 45,
                suggestions(MuscleGroup.CHEST, MuscleGroup.BACK),
                Map.of(), Map.of());

        assertThat(workout.selectedMuscleGroups()).containsExactly(MuscleGroup.CHEST);
    }

    @Test
    void exerciseCoveringMostMusclesComesFirst() {
        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.MUSCLE_GROWTH), 45, suggestions(MuscleGroup.CHEST), chestPool, progression);

        assertThat(workout.exercises()).isNotEmpty();
        assertThat(workout.exercises().get(0).exerciseTypeId()).isEqualTo(1L);
    }

    @Test
    void thirtyMinuteWorkoutsHaveAtMostTwoExercises() {
        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.MUSCLE_GROWTH), 30, suggestions(MuscleGroup.CHEST), chestPool, progression);

        assertThat(workout.exercises()).hasSize(2);
    }

    @Test
    void stableStraightSetsGetThreeSetsAndShortRest() {
        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.MUSCLE_GROWTH), 45, suggestions(MuscleGroup.CHEST),
                Map.of(MuscleGroup.CHEST, List.of(flyes)), Map.of(2L, stable(2L)));

        GeneratedExerciseDto exercise = workout.exercises().get(0);
        assertThat(exercise.sets()).isEqualTo(3);
        assertThat(exercise.restSeconds()).isEqualTo(90);
        assertThat(exercise.suggestedWeight()).isEqualTo(50.0);
        assertThat(exercise.targetReps()).isEqualTo(10);
    }

    @Test
    void unsettledPyramidSetsGetAnExtraSetAndLongerRest() {
        ExerciseProgressionRecommendation unsettled = new ExerciseProgressionRecommendation(
                2L, "Exercise 2", 50, 10, 50, false, false, false, false, 10, "test");

        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.MUSCLE_GROWTH), 45, suggestions(MuscleGroup.CHEST),
                Map.of(MuscleGroup.CHEST, List.of(flyes)), Map.of(2L, unsettled));

        GeneratedExerciseDto exercise = workout.exercises().get(0);
        assertThat(exercise.sets()).isEqualTo(4);
        assertThat(exercise.restSeconds()).isEqualTo(120);
    }

    @Test
    void exercisesWithoutProgressionDataAreSkipped() {
        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.MUSCLE_GROWTH), 45, suggestions(MuscleGroup.CHEST),
                chestPool, Map.of(3L, stable(3L)));

        assertThat(workout.exercises())
                .extracting(GeneratedExerciseDto::exerciseTypeId)
                .containsExactly(3L);
    }

    @Test
    void exercisesThatDoNotFitTheTimeAreSkipped() {
        // 3 sets of 30 s plus 2 rests of 90 s need 270 s, more than the 60 s available.
        GeneratedWorkoutResponse workout = system.generateWorkout(
                user(UserGoal.MUSCLE_GROWTH), 1, suggestions(MuscleGroup.CHEST), chestPool, progression);

        assertThat(workout.exercises()).isEmpty();
    }

    private static User user(UserGoal goal) {
        User user = new User();
        user.setGoal(goal);
        return user;
    }

    private static SuggestionResponseDto suggestions(MuscleGroup... groups) {
        return new SuggestionResponseDto(Arrays.stream(groups)
                .map(group -> new MuscleSuggestionDto(group, "test"))
                .toList());
    }

    private static ExerciseType exercise(Long id, int setDurationSeconds, TargetMuscle... targets) {
        ExerciseType exercise = new ExerciseType();
        exercise.setId(id);
        exercise.setName("Exercise " + id);
        exercise.setAverageSetDurationSeconds(setDurationSeconds);
        exercise.setTargetMuscles(List.of(targets));
        return exercise;
    }

    private static ExerciseProgressionRecommendation stable(Long id) {
        return new ExerciseProgressionRecommendation(
                id, "Exercise " + id, 50, 10, 50, false, true, false, true, 10, "test");
    }
}
