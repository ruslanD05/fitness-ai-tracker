package com.ruslandontsov.fitness.security;

import com.ruslandontsov.fitness.model.*;
import com.ruslandontsov.fitness.repository.MuscleRecoveryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class MuscleRecoveryService {

    private static final double DECAY_RATE = 0.85;
    private static final double SORE_THRESHOLD = 60.0;
    private static final double RECOVERING_THRESHOLD = 25.0;

    private final MuscleRecoveryRepository recoveryRepository;

    public MuscleRecoveryService(MuscleRecoveryRepository recoveryRepository) {
        this.recoveryRepository = recoveryRepository;
    }

    //TODO remove rir and update fatigue formula
    public void recoveryUpdate(User user, SetEntry setEntry){
        addFatigue(user, setEntry.getExerciseType().getMuscleGroup(), setEntry.getWeight(), setEntry.getReps(), 1);
    }

    private void addFatigue(User user, MuscleGroup muscleGroup, double weight, int reps, int rir) {
        MuscleRecovery record = recoveryRepository
                .findByUserIdAndMuscleGroup(user.getId(), muscleGroup)
                .orElseThrow(() -> new RuntimeException("Recovery record not found for muscle: " + muscleGroup));

        // First apply decay for days since last update
        double decayed = applyDecay(record.getFatigueScore(), record.getLastUpdatedDate());

        // Then add new fatigue from this set
        double rirMultiplier = getRirMultiplier(rir);
        double experienceMultiplier = getExperienceMultiplier(user.getExperienceLevel());
        // TODO: normalize load by muscle group baseline to fix cross-muscle comparison
        // Current formula (weight × reps × multipliers) produces values in thousands
        // and unfairly penalises compound movements vs isolation work
        double newFatigue = weight * reps * rirMultiplier * experienceMultiplier;

        record.setFatigueScore(decayed + newFatigue);
        record.setLastUpdatedDate(LocalDate.now());
        recoveryRepository.save(record);
    }

    public String computeStatus(MuscleRecovery record) {
        if (record == null) return "fresh";
        double current = applyDecay(record.getFatigueScore(), record.getLastUpdatedDate());
        if (current > SORE_THRESHOLD) return "sore";
        if (current > RECOVERING_THRESHOLD) return "recovering";
        return "fresh";
    }

    public void initializeRecoveryForUser(User user) {
        for (MuscleGroup muscleGroup : MuscleGroup.values()) {
            MuscleRecovery record = new MuscleRecovery();
            record.setUser(user);
            record.setMuscleGroup(muscleGroup);
            record.setFatigueScore(0.0);
            record.setLastUpdatedDate(null);
            recoveryRepository.save(record);
        }
    }

    public double getCurrentFatigue(MuscleRecovery record) {
        if (record == null) return 0.0;
        return applyDecay(record.getFatigueScore(), record.getLastUpdatedDate());
    }

    private double applyDecay(double score, LocalDate lastUpdated) {
        if (lastUpdated == null) return 0.0;
        long daysSince = ChronoUnit.DAYS.between(lastUpdated, LocalDate.now());
        return score * Math.pow(DECAY_RATE, daysSince);
    }

    private double getRirMultiplier(int rir) {
        if (rir <= 0) return 1.0;
        if (rir == 1) return 0.9;
        if (rir == 2) return 0.8;
        return 0.7;
    }

    private double getExperienceMultiplier(UserExperience experience) {
        return switch (experience) {
            case NO_EXPERIENCE -> 1.5;
            case BEGINNER -> 1.3;
            case INTERMEDIATE -> 1.0;
            case ADVANCED -> 0.8;
        };
    }
}
