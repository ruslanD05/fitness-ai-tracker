package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.dto.RecoveryResponseDto;
import com.ruslandontsov.fitness.dto.SuggestionResponseDto;
import com.ruslandontsov.fitness.model.*;
import com.ruslandontsov.fitness.algorithm.MuscleRecommendationSystem;
import com.ruslandontsov.fitness.repository.MuscleRecoveryRepository;
import com.ruslandontsov.fitness.repository.SetEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
public class MuscleRecoveryService {

    private final MuscleRecoveryRepository muscleRecoveryRepository;
    private final SetEntryRepository setEntryRepository;
    private final MuscleRecommendationSystem muscleRecommendationSystem;

    public MuscleRecoveryService(
            MuscleRecoveryRepository muscleRecoveryRepository,
            SetEntryRepository setEntryRepository,
            MuscleRecommendationSystem muscleRecommendationSystem
    ) {
        this.muscleRecoveryRepository = muscleRecoveryRepository;
        this.setEntryRepository = setEntryRepository;
        this.muscleRecommendationSystem = muscleRecommendationSystem;
    }


    public void initializeRecoveryForUser(User user) {
        for (MuscleGroup muscleGroup : MuscleGroup.values()) {
            MuscleRecovery record = new MuscleRecovery();
            record.setUser(user);
            record.setMuscleGroup(muscleGroup);
            record.setFatigueScore(0.0);
            record.setLastUpdatedDate(null);
            muscleRecoveryRepository.save(record);
        }
    }

    @Transactional
    public void updateRecoveryAfterCompletedSet(User user, SetEntry setEntry) {
        ExerciseType exerciseType = setEntry.getExerciseType();
        MuscleGroup muscleGroup = exerciseType.getMuscleGroup();

        MuscleRecovery recovery = muscleRecoveryRepository
                .findByUserAndMuscleGroup(user, muscleGroup)
                .orElseThrow(() -> new IllegalStateException(
                        "Recovery row not found for user " + user.getId() + " and muscle " + muscleGroup
                ));

        int completedSetCount = setEntryRepository.countCompletedSetsByUserAndExerciseType(user.getId(), exerciseType.getId());
        Double userAverageLoad = setEntryRepository.findAverageLoadByUserAndExerciseType(user.getId(), exerciseType.getId());

        double baselineLoad = muscleRecommendationSystem.resolveBaselineLoad(
                exerciseType.getBaselineLoad(),
                userAverageLoad,
                completedSetCount
        );

        double decayedFatigue = muscleRecommendationSystem.getCurrentFatigue(recovery);
        double contribution = muscleRecommendationSystem.computeFatigueContribution(user, setEntry, baselineLoad);

        double newFatigue = Math.min(decayedFatigue + contribution, 1.0);

        recovery.setFatigueScore(newFatigue);
        recovery.setLastUpdatedDate(LocalDate.now());

        muscleRecoveryRepository.save(recovery);
    }

    @Transactional(readOnly = true)
    public List<RecoveryResponseDto> getRecoveryMapForUser(User user) {
        return muscleRecoveryRepository.findByUser(user).stream()
                .sorted(Comparator.comparing(r -> r.getMuscleGroup().name()))
                .map(recovery -> {
                    double currentFatigue = muscleRecommendationSystem.getCurrentFatigue(recovery);
                    String status = muscleRecommendationSystem.computeStatus(recovery);

                    return new RecoveryResponseDto(
                            recovery.getMuscleGroup(),
                            currentFatigue,
                            status
                    );
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public SuggestionResponseDto getSuggestionForUser(User user) {
        List<MuscleRecovery> recoveries = muscleRecoveryRepository.findByUser(user);

        MuscleRecovery bestRecovery = muscleRecommendationSystem
                .suggestNextMuscle(recoveries)
                .orElseThrow(() -> new IllegalStateException("No recovery data found for user " + user.getId()));

        return new SuggestionResponseDto(
                bestRecovery.getMuscleGroup(),
                muscleRecommendationSystem.buildSuggestionReason(bestRecovery)
        );
    }
}
