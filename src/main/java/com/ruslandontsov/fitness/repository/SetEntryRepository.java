package com.ruslandontsov.fitness.repository;

import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SetEntryRepository extends JpaRepository<SetEntry, Long> {
    List<SetEntry> findByWorkoutId(Long workoutId);
}
