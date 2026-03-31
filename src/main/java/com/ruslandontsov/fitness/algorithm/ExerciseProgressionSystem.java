package com.ruslandontsov.fitness.algorithm;


import com.ruslandontsov.fitness.dto.ExerciseProgressionRecommendation;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.SetEntry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExerciseProgressionSystem {

    private static final int LOOKBACK_SETS = 6;
    private static final int MIN_SETS_FOR_STRONG_SIGNAL = 3;

    public int getLookbackSets() {
        return LOOKBACK_SETS;
    }

    public ExerciseProgressionRecommendation recommend(
            ExerciseType exerciseType,
            List<SetEntry> recentSets
    ) {
        if (recentSets == null || recentSets.isEmpty()) {
            double baselineLoad = exerciseType.getBaselineLoad() != null ? exerciseType.getBaselineLoad() : 0.0;

            return new ExerciseProgressionRecommendation(
                    exerciseType.getId(),
                    exerciseType.getName(),
                    0.0,
                    0.0,
                    baselineLoad,
                    false,
                    false,
                    true,
                    "8-10",
                    "No history yet - start from baseline"
            );
        }

        List<SetEntry> usableSets = recentSets.stream()
                .limit(LOOKBACK_SETS)
                .toList();

        double avgWeight = usableSets.stream()
                .mapToDouble(SetEntry::getWeight)
                .average()
                .orElse(0.0);

        double avgReps = usableSets.stream()
                .mapToInt(SetEntry::getReps)
                .average()
                .orElse(0.0);

        List<SetEntry> lastThree = usableSets.stream()
                .limit(Math.min(MIN_SETS_FOR_STRONG_SIGNAL, usableSets.size()))
                .toList();

        boolean stableWeight = areWeightsStable(lastThree);
        boolean stableReps = areRepsStable(lastThree);

        if (lastThree.size() >= MIN_SETS_FOR_STRONG_SIGNAL && stableWeight && stableReps) {
            int reps = lastThree.getFirst().getReps();
            double weight = lastThree.getFirst().getWeight();

            if (reps >= 12) {
                double suggestedWeight = increaseWeight(weight);

                return new ExerciseProgressionRecommendation(
                        exerciseType.getId(),
                        exerciseType.getName(),
                        round(avgWeight),
                        round(avgReps),
                        suggestedWeight,
                        true,
                        false,
                        false,
                        "8-10",
                        "Stable performance at 12+ reps - increase weight and reset reps"
                );
            }

            return new ExerciseProgressionRecommendation(
                    exerciseType.getId(),
                    exerciseType.getName(),
                    round(avgWeight),
                    round(avgReps),
                    weight,
                    false,
                    true,
                    false,
                    (reps + 1) + "-" + (reps + 2),
                    "Stable performance below 12 reps - increase reps"
            );
        }

        return new ExerciseProgressionRecommendation(
                exerciseType.getId(),
                exerciseType.getName(),
                round(avgWeight),
                round(avgReps),
                round(avgWeight),
                false,
                false,
                true,
                buildDefaultRepRange(avgReps),
                "Recent sets are not stable enough - keep current load"
        );
    }

    private boolean areWeightsStable(List<SetEntry> sets) {
        if (sets.isEmpty()) return false;

        double first = sets.getFirst().getWeight();
        return sets.stream().allMatch(se -> Double.compare(se.getWeight(), first) == 0);
    }

    private boolean areRepsStable(List<SetEntry> sets) {
        if (sets.isEmpty()) return false;

        int first = sets.getFirst().getReps();
        return sets.stream().allMatch(se -> se.getReps() == first);
    }

    private double increaseWeight(double currentWeight) {
        if (currentWeight <= 0) return 0.0;
        if (currentWeight <= 10) return round(currentWeight + 1.0);
        if (currentWeight <= 25) return round(currentWeight + 2.0);
        return round(currentWeight + 2.5);
    }

    private String buildDefaultRepRange(double avgReps) {
        if (avgReps < 8) return "8-10";
        if (avgReps < 12) return "10-12";
        return "8-10";
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}