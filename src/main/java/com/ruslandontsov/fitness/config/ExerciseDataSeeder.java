package com.ruslandontsov.fitness.config;

import com.ruslandontsov.fitness.model.ExerciseType;
import com.ruslandontsov.fitness.model.MuscleGroup;
import com.ruslandontsov.fitness.model.TargetMuscle;
import com.ruslandontsov.fitness.repository.ExerciseTypeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExerciseDataSeeder implements CommandLineRunner {

    private final ExerciseTypeRepository exerciseTypeRepository;

    public ExerciseDataSeeder(ExerciseTypeRepository exerciseTypeRepository) {
        this.exerciseTypeRepository = exerciseTypeRepository;
    }

    @Override
    public void run(String... args) {
        if (exerciseTypeRepository.count() > 0) {
            return;
        }

        exerciseTypeRepository.saveAll(List.of(
                exercise(
                        "Bench Press",
                        MuscleGroup.CHEST,
                        List.of(TargetMuscle.MID_CHEST, TargetMuscle.FRONT_DELTS, TargetMuscle.TRICEPS),
                        45,
                        400.0,
                        1.0
                ),
                exercise(
                        "Incline Dumbbell Press",
                        MuscleGroup.CHEST,
                        List.of(TargetMuscle.UPPER_CHEST, TargetMuscle.FRONT_DELTS, TargetMuscle.TRICEPS),
                        50,
                        280.0,
                        1.05
                ),
                exercise(
                        "Chest Fly",
                        MuscleGroup.CHEST,
                        List.of(TargetMuscle.MID_CHEST, TargetMuscle.UPPER_CHEST, TargetMuscle.FRONT_DELTS),
                        40,
                        180.0,
                        1.1
                ),
                exercise(
                        "Pull Up",
                        MuscleGroup.BACK,
                        List.of(TargetMuscle.LATS, TargetMuscle.BICEPS, TargetMuscle.RHOMBOIDS, TargetMuscle.FOREARMS),
                        40,
                        80.0,
                        1.0
                ),
                exercise(
                        "Lat Pulldown",
                        MuscleGroup.BACK,
                        List.of(TargetMuscle.LATS, TargetMuscle.BICEPS, TargetMuscle.RHOMBOIDS, TargetMuscle.FOREARMS),
                        45,
                        300.0,
                        1.0
                ),
                exercise(
                        "Barbell Row",
                        MuscleGroup.BACK,
                        List.of(TargetMuscle.MIDDLE_BACK, TargetMuscle.LATS, TargetMuscle.RHOMBOIDS, TargetMuscle.BICEPS, TargetMuscle.ERECTOR_SPINAE),
                        50,
                        350.0,
                        1.05
                ),
                exercise(
                        "Deadlift",
                        MuscleGroup.BACK,
                        List.of(TargetMuscle.LOWER_BACK, TargetMuscle.TRAPS, TargetMuscle.HAMSTRINGS, TargetMuscle.GLUTES, TargetMuscle.ERECTOR_SPINAE),
                        60,
                        600.0,
                        1.15
                ),
                exercise(
                        "Squat",
                        MuscleGroup.LEGS,
                        List.of(TargetMuscle.QUADS, TargetMuscle.GLUTES, TargetMuscle.ADDUCTORS, TargetMuscle.TRANSVERSE_ABDOMINIS, TargetMuscle.ERECTOR_SPINAE),
                        60,
                        560.0,
                        1.1
                ),
                exercise(
                        "Leg Press",
                        MuscleGroup.LEGS,
                        List.of(TargetMuscle.QUADS, TargetMuscle.GLUTES, TargetMuscle.HAMSTRINGS),
                        50,
                        800.0,
                        1.0
                ),
                exercise(
                        "Romanian Deadlift",
                        MuscleGroup.LEGS,
                        List.of(TargetMuscle.HAMSTRINGS, TargetMuscle.GLUTES, TargetMuscle.LOWER_BACK, TargetMuscle.ERECTOR_SPINAE),
                        55,
                        420.0,
                        1.1
                ),
                exercise(
                        "Leg Extension",
                        MuscleGroup.LEGS,
                        List.of(TargetMuscle.QUADS),
                        40,
                        250.0,
                        1.2
                ),
                exercise(
                        "Leg Curl",
                        MuscleGroup.LEGS,
                        List.of(TargetMuscle.HAMSTRINGS),
                        40,
                        220.0,
                        1.15
                ),
                exercise(
                        "Overhead Press",
                        MuscleGroup.SHOULDERS,
                        List.of(TargetMuscle.FRONT_DELTS, TargetMuscle.TRICEPS, TargetMuscle.SIDE_DELTS, TargetMuscle.TRANSVERSE_ABDOMINIS),
                        45,
                        240.0,
                        1.0
                ),
                exercise(
                        "Lateral Raise",
                        MuscleGroup.SHOULDERS,
                        List.of(TargetMuscle.SIDE_DELTS),
                        35,
                        100.0,
                        1.15
                ),
                exercise(
                        "Rear Delt Fly",
                        MuscleGroup.SHOULDERS,
                        List.of(TargetMuscle.REAR_DELTS, TargetMuscle.RHOMBOIDS, TargetMuscle.UPPER_BACK),
                        35,
                        90.0,
                        1.1
                ),
                exercise(
                        "Barbell Curl",
                        MuscleGroup.ARMS,
                        List.of(TargetMuscle.BICEPS, TargetMuscle.FOREARMS),
                        35,
                        120.0,
                        1.0
                ),
                exercise(
                        "Hammer Curl",
                        MuscleGroup.ARMS,
                        List.of(TargetMuscle.BRACHIALIS, TargetMuscle.BICEPS, TargetMuscle.FOREARMS),
                        35,
                        140.0,
                        1.05
                ),
                exercise(
                        "Tricep Pushdown",
                        MuscleGroup.ARMS,
                        List.of(TargetMuscle.TRICEPS),
                        35,
                        180.0,
                        1.0
                ),
                exercise(
                        "Skull Crusher",
                        MuscleGroup.ARMS,
                        List.of(TargetMuscle.TRICEPS),
                        40,
                        140.0,
                        1.1
                ),
                exercise(
                        "Cable Crunch",
                        MuscleGroup.CORE,
                        List.of(TargetMuscle.UPPER_ABS, TargetMuscle.LOWER_ABS),
                        30,
                        160.0,
                        1.0
                )
        ));
    }

    private ExerciseType exercise(
            String name,
            MuscleGroup primaryMuscleGroup,
            List<TargetMuscle> targetMuscles,
            int averageSetDurationSeconds,
            double baselineLoad,
            double recoveryImpactMultiplier
    ) {
        ExerciseType e = new ExerciseType();
        e.setName(name);
        e.setPrimaryMuscleGroup(primaryMuscleGroup);
        e.setTargetMuscles(targetMuscles);
        e.setAverageSetDurationSeconds(averageSetDurationSeconds);
        e.setBaselineLoad(baselineLoad);
        e.setRecoveryImpactMultiplier(recoveryImpactMultiplier);
        return e;
    }
}