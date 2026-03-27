package com.ruslandontsov.fitness.algorithm;

import com.ruslandontsov.fitness.model.MuscleRecovery;
import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.UserExperience;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
public class MuscleRecommendationSystem {

    private static final double DECAY_RATE = 0.85;
    private static final double SORE_THRESHOLD = 0.8;
    private static final double RECOVERING_THRESHOLD = 0.3;

    public double computeFatigueContribution(
            User user,
            SetEntry setEntry,
            double baselineLoad
    ) {
        double actualLoad = setEntry.getWeight() * setEntry.getReps();

        if (baselineLoad <= 0) {
            baselineLoad = actualLoad;
        }

        double relativeLoad = actualLoad <= baselineLoad ? actualLoad / baselineLoad: 1 + (actualLoad-baselineLoad)/(baselineLoad/2); //if user went above baseline, fatigue accumulation doubles for every extra kg
        relativeLoad = Math.min(relativeLoad, 1.5);
        double experienceMultiplier = getExperienceMultiplier(user.getExperienceLevel());

        return relativeLoad * experienceMultiplier;
    }

    private double applyDecay(double fatigueScore, LocalDate lastUpdatedDate) {
        if (lastUpdatedDate == null) {
            return 0.0;
        }

        long daysSince = ChronoUnit.DAYS.between(lastUpdatedDate, LocalDate.now());
        return fatigueScore * Math.pow(DECAY_RATE, daysSince);
    }

    public double getCurrentFatigue(MuscleRecovery recovery) {
        if (recovery == null) {
            return 0.0;
        }

        return applyDecay(recovery.getFatigueScore(), recovery.getLastUpdatedDate());
    }

    public String computeStatus(MuscleRecovery recovery) {
        double currentFatigue = getCurrentFatigue(recovery);

        if (currentFatigue >= SORE_THRESHOLD) {
            return "sore";
        }
        if (currentFatigue >= RECOVERING_THRESHOLD) {
            return "recovering";
        }
        return "fresh";
    }

    private boolean isColdStart(int completedSetCount) {
        return completedSetCount < 3;
    }

    public double resolveBaselineLoad(
            double exerciseBaselineLoad,
            Double userAverageLoad,
            int completedSetCount
    ) {
        if (!isColdStart(completedSetCount) && userAverageLoad != null && userAverageLoad > 0) {
            return userAverageLoad;
        }

        else if (exerciseBaselineLoad > 0) {
            return exerciseBaselineLoad;
        }
        else {
            return 50.0;
        }
    }

    public Optional<MuscleRecovery> suggestNextMuscle(List<MuscleRecovery> recoveries) {
        return recoveries.stream()
                .filter(r -> getCurrentFatigue(r) < SORE_THRESHOLD)
                .min(Comparator.comparingDouble(this::getCurrentFatigue));
    }

    public String buildSuggestionReason(MuscleRecovery recovery) {
        double fatigue = getCurrentFatigue(recovery);

        if (fatigue <= 0.05) {
            return "Never trained or fully recovered";
        }

        return "Fatigue score " + Math.round(fatigue * 100.0) / 100.0 + " — most recovered";
    }

    private double getExperienceMultiplier(UserExperience experience) {
        return switch (experience) {
            case NO_EXPERIENCE -> 0.1;
            case BEGINNER -> 0.08;
            case INTERMEDIATE -> 0.06;
            case ADVANCED -> 0.05;
        };
    }
}