package com.ruslandontsov.fitness.repository;

import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.MuscleRecovery;
import com.ruslandontsov.fitness.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MuscleRecoveryRepository extends JpaRepository<MuscleRecovery,Long> {
    List<MuscleRecovery> findByUserId(Long userId);
    Optional<MuscleRecovery> findByUserIdAndMuscleGroup(Long userId, MuscleGroup muscleGroup);
    Optional<MuscleRecovery> findByUserAndMuscleGroup(User user, MuscleGroup muscleGroup);
    List<MuscleRecovery> findByUser(User user);
}