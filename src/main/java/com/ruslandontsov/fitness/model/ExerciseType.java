package com.ruslandontsov.fitness.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "exercise_types")
public class ExerciseType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Enumerated(EnumType.STRING)
    private List<MuscleGroup> muscleGroups;
    @Enumerated(EnumType.STRING)
    private List<TargetMuscle> targetMuscles;

    private Integer averageSetDurationSeconds;

    private Double baselineLoad;
}