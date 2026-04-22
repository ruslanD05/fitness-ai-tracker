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
    private MuscleGroup primaryMuscleGroup;

    @ElementCollection(targetClass = TargetMuscle.class)
    @CollectionTable(name = "exercise_type_target_muscles", joinColumns = @JoinColumn(name = "exercise_type_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "target_muscle")
    @OrderColumn(name = "priority_index")
    private List<TargetMuscle> targetMuscles;

    private Integer averageSetDurationSeconds;

    private Double baselineLoad;

    private Double recoveryImpactMultiplier;
}