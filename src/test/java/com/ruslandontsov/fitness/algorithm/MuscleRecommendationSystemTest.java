package com.ruslandontsov.fitness.algorithm;

import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.MuscleRecovery;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.UserExperience;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MuscleRecommendationSystemTest {

    private final MuscleRecommendationSystem system = new MuscleRecommendationSystem();

    @Test
    void fatigueAtBaselineIsScaledByExperienceAndExerciseImpact() {
        User beginner = user(UserExperience.BEGINNER);

        double fatigue = system.computeFatigueContribution(beginner, set(50, 10, 1.0), 500);

        assertThat(fatigue).isCloseTo(0.08, within(1e-9));
    }

    @Test
    void fatigueGrowsFasterAboveBaseline() {
        User beginner = user(UserExperience.BEGINNER);

        // 600 load vs 500 baseline -> relative load 1 + 100 / 250 = 1.4
        double fatigue = system.computeFatigueContribution(beginner, set(60, 10, 1.0), 500);

        assertThat(fatigue).isCloseTo(1.4 * 0.08, within(1e-9));
    }

    @Test
    void relativeLoadIsCappedAtOnePointFive() {
        User beginner = user(UserExperience.BEGINNER);

        double fatigue = system.computeFatigueContribution(beginner, set(100, 10, 1.0), 500);

        assertThat(fatigue).isCloseTo(1.5 * 0.08, within(1e-9));
    }

    @Test
    void missingBaselineFallsBackToTheSetItself() {
        User advanced = user(UserExperience.ADVANCED);

        double fatigue = system.computeFatigueContribution(advanced, set(80, 5, 2.0), 0);

        assertThat(fatigue).isCloseTo(0.05 * 2.0, within(1e-9));
    }

    @Test
    void moreExperiencedUsersAccumulateLessFatigue() {
        SetEntry entry = set(50, 10, 1.0);

        double novice = system.computeFatigueContribution(user(UserExperience.NO_EXPERIENCE), entry, 500);
        double advanced = system.computeFatigueContribution(user(UserExperience.ADVANCED), entry, 500);

        assertThat(novice).isGreaterThan(advanced);
    }

    @Test
    void fatigueDecaysEachDay() {
        MuscleRecovery recovery = recovery(MuscleGroup.CHEST, 1.0, LocalDate.now().minusDays(2));

        assertThat(system.getCurrentFatigue(recovery)).isCloseTo(0.85 * 0.85, within(1e-9));
    }

    @Test
    void missingRecoveryDataMeansNoFatigue() {
        assertThat(system.getCurrentFatigue(null)).isZero();
        assertThat(system.getCurrentFatigue(recovery(MuscleGroup.BACK, 0.9, null))).isZero();
    }

    @Test
    void statusFollowsFatigueThresholds() {
        LocalDate today = LocalDate.now();

        assertThat(system.computeStatus(recovery(MuscleGroup.LEGS, 0.9, today))).isEqualTo("sore");
        assertThat(system.computeStatus(recovery(MuscleGroup.LEGS, 0.8, today))).isEqualTo("sore");
        assertThat(system.computeStatus(recovery(MuscleGroup.LEGS, 0.5, today))).isEqualTo("recovering");
        assertThat(system.computeStatus(recovery(MuscleGroup.LEGS, 0.1, today))).isEqualTo("fresh");
    }

    @Test
    void baselineUsesUserHistoryOnlyAfterColdStart() {
        assertThat(system.resolveBaselineLoad(40, 60.0, 5)).isEqualTo(60.0);
        assertThat(system.resolveBaselineLoad(40, 60.0, 2)).isEqualTo(40.0);
        assertThat(system.resolveBaselineLoad(40, null, 10)).isEqualTo(40.0);
        assertThat(system.resolveBaselineLoad(0, null, 0)).isEqualTo(50.0);
    }

    @Test
    void suggestionsSkipSoreMusclesAndPutTheFreshestFirst() {
        LocalDate today = LocalDate.now();
        MuscleRecovery sore = recovery(MuscleGroup.CHEST, 0.95, today);
        MuscleRecovery recovering = recovery(MuscleGroup.BACK, 0.4, today);
        MuscleRecovery fresh = recovery(MuscleGroup.LEGS, 0.0, today);

        List<MuscleRecovery> suggestions = system.suggestNextMuscles(List.of(sore, recovering, fresh));

        assertThat(suggestions).containsExactly(fresh, recovering);
    }

    @Test
    void suggestionReasonDescribesFatigue() {
        LocalDate today = LocalDate.now();

        assertThat(system.buildSuggestionReason(recovery(MuscleGroup.ARMS, 0.0, today)))
                .isEqualTo("Never trained or fully recovered");
        assertThat(system.buildSuggestionReason(recovery(MuscleGroup.ARMS, 0.456, today)))
                .startsWith("Fatigue score 0.46");
    }

    private static User user(UserExperience experience) {
        User user = new User();
        user.setExperienceLevel(experience);
        return user;
    }

    private static SetEntry set(double weight, int reps, double recoveryImpact) {
        ExerciseType exercise = new ExerciseType();
        exercise.setRecoveryImpactMultiplier(recoveryImpact);

        SetEntry entry = new SetEntry();
        entry.setWeight(weight);
        entry.setReps(reps);
        entry.setExerciseType(exercise);
        return entry;
    }

    private static MuscleRecovery recovery(MuscleGroup group, double fatigue, LocalDate lastUpdated) {
        MuscleRecovery recovery = new MuscleRecovery();
        recovery.setMuscleGroup(group);
        recovery.setFatigueScore(fatigue);
        recovery.setLastUpdatedDate(lastUpdated);
        return recovery;
    }
}
