package com.ruslandontsov.fitness.algorithm;

import com.ruslandontsov.fitness.dto.ExerciseProgressionRecommendation;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.SetEntry;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExerciseProgressionSystemTest {

    private final ExerciseProgressionSystem system = new ExerciseProgressionSystem();
    private final ExerciseType benchPress = exercise(1L, "Bench Press", 40.0);

    @Test
    void withoutEnoughHistoryStartFromBaseline() {
        ExerciseProgressionRecommendation rec = system.recommend(benchPress, List.of(set(60, 10)));

        assertThat(rec.keepSame()).isTrue();
        assertThat(rec.suggestedWeight()).isEqualTo(40.0);
        assertThat(rec.targetReps()).isEqualTo(8);
        assertThat(rec.reason()).isEqualTo("No history yet - start from baseline");
    }

    @Test
    void missingBaselineStartsAtZero() {
        ExerciseType noBaseline = exercise(2L, "Curl", null);

        ExerciseProgressionRecommendation rec = system.recommend(noBaseline, List.of());

        assertThat(rec.suggestedWeight()).isZero();
    }

    @Test
    void stableSetsAtTwelveRepsIncreaseWeight() {
        ExerciseProgressionRecommendation rec =
                system.recommend(benchPress, List.of(set(60, 12), set(60, 12), set(60, 12)));

        assertThat(rec.increaseWeight()).isTrue();
        assertThat(rec.suggestedWeight()).isEqualTo(62.5);
        assertThat(rec.straightSets()).isTrue();
    }

    @Test
    void stableSetsBelowTwelveRepsAddARep() {
        ExerciseProgressionRecommendation rec =
                system.recommend(benchPress, List.of(set(60, 10), set(60, 10), set(60, 10)));

        assertThat(rec.increaseReps()).isTrue();
        assertThat(rec.suggestedWeight()).isEqualTo(60.0);
        assertThat(rec.targetReps()).isEqualTo(11);
    }

    @Test
    void weightIncrementDependsOnCurrentLoad() {
        assertThat(system.recommend(benchPress, repeat(10, 12, 3)).suggestedWeight()).isEqualTo(11.0);
        assertThat(system.recommend(benchPress, repeat(20, 12, 3)).suggestedWeight()).isEqualTo(22.0);
        assertThat(system.recommend(benchPress, repeat(30, 12, 3)).suggestedWeight()).isEqualTo(32.5);
    }

    @Test
    void unevenTopSetsKeepTheCurrentLoad() {
        // Not monotonic, so the style is STABLE, but the top sets differ by 2.5 kg.
        ExerciseProgressionRecommendation rec =
                system.recommend(benchPress, List.of(set(55, 10), set(60, 10), set(57.5, 10)));

        assertThat(rec.keepSame()).isTrue();
        assertThat(rec.suggestedWeight()).isEqualTo(58.75);
    }

    @Test
    void pyramidWithRepeatedTopSetAtEightRepsIncreasesWeight() {
        ExerciseProgressionRecommendation rec =
                system.recommend(benchPress, List.of(set(40, 12), set(60, 8), set(60, 8)));

        assertThat(rec.straightSets()).isFalse();
        assertThat(rec.increaseWeight()).isTrue();
        assertThat(rec.suggestedWeight()).isEqualTo(62.5);
    }

    @Test
    void pyramidWithRepeatedTopSetBelowEightRepsAddsARep() {
        ExerciseProgressionRecommendation rec =
                system.recommend(benchPress, List.of(set(40, 10), set(60, 6), set(60, 6)));

        assertThat(rec.straightSets()).isFalse();
        assertThat(rec.increaseReps()).isTrue();
        assertThat(rec.suggestedWeight()).isEqualTo(60.0);
        assertThat(rec.targetReps()).isEqualTo(7);
    }

    @Test
    void pyramidWithSingleTopSetKeepsTheCurrentLoad() {
        ExerciseProgressionRecommendation rec =
                system.recommend(benchPress, List.of(set(40, 10), set(50, 8), set(60, 5)));

        assertThat(rec.straightSets()).isFalse();
        assertThat(rec.keepSame()).isTrue();
    }

    @Test
    void onlyTheMostRecentSetsAreConsidered() {
        List<SetEntry> history = new ArrayList<>(repeat(60, 10, system.getLookbackSets()));
        history.addAll(repeat(200, 1, 3));

        ExerciseProgressionRecommendation rec = system.recommend(benchPress, history);

        assertThat(rec.increaseReps()).isTrue();
        assertThat(rec.suggestedWeight()).isEqualTo(60.0);
    }

    private static ExerciseType exercise(Long id, String name, Double baselineLoad) {
        ExerciseType exercise = new ExerciseType();
        exercise.setId(id);
        exercise.setName(name);
        exercise.setBaselineLoad(baselineLoad);
        return exercise;
    }

    private static SetEntry set(double weight, int reps) {
        SetEntry entry = new SetEntry();
        entry.setWeight(weight);
        entry.setReps(reps);
        return entry;
    }

    private static List<SetEntry> repeat(double weight, int reps, int count) {
        List<SetEntry> sets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sets.add(set(weight, reps));
        }
        return sets;
    }
}
