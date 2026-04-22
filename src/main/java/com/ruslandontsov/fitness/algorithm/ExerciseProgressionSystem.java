package com.ruslandontsov.fitness.algorithm;


import com.ruslandontsov.fitness.dto.ExerciseProgressionRecommendation;
import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.ProgressionStyle;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExerciseProgressionSystem{

    private static final int LOOKBACK_SETS = 9;
    private static final int MIN_SETS_FOR_STRONG_SIGNAL = 3;

    public int getLookbackSets() {
        return LOOKBACK_SETS;
    }

    public ExerciseProgressionRecommendation recommend(
            ExerciseType exerciseType,
            List<SetEntry> userSets
    ) {
        if (userSets == null || userSets.isEmpty() || userSets.size() < MIN_SETS_FOR_STRONG_SIGNAL) {
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
                    true,
                    8,
                    "No history yet - start from baseline"
            );
        }

        List<SetEntry> usableSets = userSets.stream()
                .limit(LOOKBACK_SETS)
                .toList();

        List<SetEntry> performanceSets = extractPerformanceSets(usableSets);

        double avgWeight = performanceSets.stream()
                .mapToDouble(SetEntry::getWeight)
                .average()
                .orElse(0.0);

        double avgReps = performanceSets.stream()
                .mapToInt(SetEntry::getReps)
                .average()
                .orElse(0.0);

        List<SetEntry> signalSets = performanceSets.stream()
                .limit(Math.min(MIN_SETS_FOR_STRONG_SIGNAL, performanceSets.size()))
                .toList();

        ProgressionStyle progressionStyle = detectStyle(usableSets);

        boolean stableWeight = areWeightsStable(signalSets, progressionStyle);
        boolean stableReps = areRepsStable(signalSets, progressionStyle);

        if (progressionStyle == ProgressionStyle.STABLE) {
            if (stableWeight &&  stableReps) {
                   if (avgReps >= 12) {
                    double suggestedWeight = increaseWeight(avgWeight);

                    return new ExerciseProgressionRecommendation(
                            exerciseType.getId(),
                            exerciseType.getName(),
                            round(avgWeight),
                            round(avgReps),
                            suggestedWeight,
                            true,
                            false,
                            false,
                            true,
                            (int) round(avgReps),
                            "Stable performance at 12+ reps - increase weight and reset reps"
                    );
                }

                return new ExerciseProgressionRecommendation(
                        exerciseType.getId(),
                        exerciseType.getName(),
                        round(avgWeight),
                        round(avgReps),
                        avgWeight,
                        false,
                        true,
                        false,
                        true,
                        (int) round(avgReps + 1),
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
                    true,
                    (int) round(avgReps),
                    "Recent sets are not stable enough - keep current load"
            );
        } else{
            if (stableWeight && stableReps) {
                double maxWeight = signalSets.stream().mapToDouble(SetEntry::getWeight).max().orElse(0);
                //max reps on maxWeight
                int repsMaxWeightReps = signalSets.stream().filter(se -> Math.abs(se.getWeight() - maxWeight) < 0.0001).mapToInt(SetEntry::getReps).max().orElse(0);
                double weight = signalSets.stream().mapToDouble(SetEntry::getWeight).max().orElse(0.0);

                if (repsMaxWeightReps >= 8) {
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
                            false,
                            (int) round(avgReps),
                            "Stable performance at 8+ reps - increase weight and reset reps"
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
                        false,
                        (int) round(avgReps+1),
                        "Stable performance below 8 reps - increase reps"
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
                    false,
                    (int) round(avgReps),
                    "Recent sets are not stable enough - keep current load"
            );
        }

    }

    //detect if user prefers to perform stable sets(e.g. 60x10 - 3 times) or pyramid style(e.g. 40x10, 50x8, 60x5)
    private ProgressionStyle detectStyle(List<SetEntry> sets) {
        if (sets.size() < 3) {
            return ProgressionStyle.STABLE;
        }

        double minWeight = sets.stream().mapToDouble(SetEntry::getWeight).min().orElse(0);
        double maxWeight = sets.stream().mapToDouble(SetEntry::getWeight).max().orElse(0);
        double avgWeight = sets.stream().mapToDouble(SetEntry::getWeight).average().orElse(0);

        double absoluteSpread = maxWeight - minWeight;
        double relativeSpread = avgWeight == 0 ? 0 : absoluteSpread / avgWeight;

        double minReps = sets.stream().mapToDouble(SetEntry::getReps).min().orElse(0);
        double maxReps = sets.stream().mapToDouble(SetEntry::getReps).max().orElse(0);


        boolean stableWeight = (absoluteSpread <= 5 || relativeSpread <= 0.05 || !isMonotonic(sets));
        boolean stableReps = (maxReps - minReps) <= 3;

        if (stableWeight && stableReps) {
            return ProgressionStyle.STABLE;
        }

        return ProgressionStyle.PYRAMID;
    }

    private boolean isMonotonic(List<SetEntry> sets) {
        boolean nonDecreasing = true;
        boolean nonIncreasing = true;

        for (int i = 1; i < sets.size(); i++) {
            double prev = sets.get(i - 1).getWeight();
            double curr = sets.get(i).getWeight();

            if (curr < prev) nonDecreasing = false;
            if (curr > prev) nonIncreasing = false;
        }

        return nonDecreasing || nonIncreasing;
    }

    private boolean areWeightsStable(List<SetEntry> sets, ProgressionStyle progressionStyle) {
        if (sets.isEmpty()) return false;

        if(progressionStyle == ProgressionStyle.STABLE) {
            double min = sets.stream().mapToDouble(SetEntry::getWeight).min().orElse(0.0);
            double max = sets.stream().mapToDouble(SetEntry::getWeight).max().orElse(0.0);
            // performance sets do not differ by more than 2.5 kgs
            return (max - min) < 2.5;
        } else {
            double max = sets.stream().mapToDouble(SetEntry::getWeight).max().orElse(0.0);
            long countMax = sets.stream()
                    .filter(se -> Math.abs(se.getWeight() - max) < 0.0001)
                    .count();
            return countMax >= 2;
        }
    }

    private boolean areRepsStable(List<SetEntry> sets, ProgressionStyle progressionStyle) {
        if (sets.isEmpty()) return false;

        if (progressionStyle == ProgressionStyle.STABLE) {
            int min = sets.stream().mapToInt(SetEntry::getReps).min().orElse(0);
            int max = sets.stream().mapToInt(SetEntry::getReps).max().orElse(0);
            // performance sets do not differ by more than 3 reps
            return (max - min) <= 3;
        } else {
            double maxWeight = sets.stream().mapToDouble(SetEntry::getWeight).max().orElse(0);
            //max reps on maxWeight
            int max = sets.stream().filter(se -> Math.abs(se.getWeight() - maxWeight) < 0.0001).mapToInt(SetEntry::getReps).max().orElse(0);
            int min = sets.stream().filter(se -> Math.abs(se.getWeight() - maxWeight) < 0.0001).mapToInt(SetEntry::getReps).min().orElse(0);

            return (max - min) <= 1;
        }
    }

    private double increaseWeight(double currentWeight) {
        if (currentWeight <= 0) return 0.0;
        if (currentWeight <= 10) return round(currentWeight + 1.0);
        if (currentWeight <= 25) return round(currentWeight + 2.0);
        return round(currentWeight + 2.5);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private List<SetEntry> extractPerformanceSets(List<SetEntry> sets) {
        if (sets.isEmpty()) return List.of();

        double averageWeight = sets.stream()
                .mapToDouble(SetEntry::getWeight)
                .average()
                .orElse(0.0);

        return sets.stream()
                .filter(se -> se.getWeight() >= averageWeight)
                .toList();
    }
}