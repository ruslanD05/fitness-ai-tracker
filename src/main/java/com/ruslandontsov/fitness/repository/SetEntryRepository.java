package com.ruslandontsov.fitness.repository;

import com.ruslandontsov.fitness.model.SetEntry;
import com.ruslandontsov.fitness.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SetEntryRepository extends JpaRepository<SetEntry, Long> {
    List<SetEntry> findByWorkoutId(Long workoutId);
    @Query("""
        select count(se)
        from SetEntry se
        where se.workout.user.id = :userId
          and se.exerciseType.id = :exerciseTypeId
          and se.completed = true
    """)
    int countCompletedSetsByUserAndExerciseType(
            @Param("userId") Long userId,
            @Param("exerciseTypeId") Long exerciseTypeId
    );

    @Query("""
        select avg(se.weight * se.reps)
        from SetEntry se
        where se.workout.user.id = :userId
          and se.exerciseType.id = :exerciseTypeId
          and se.completed = true
    """)
    Double findAverageLoadByUserAndExerciseType(
            @Param("userId") Long userId,
            @Param("exerciseTypeId") Long exerciseTypeId
    );
}
