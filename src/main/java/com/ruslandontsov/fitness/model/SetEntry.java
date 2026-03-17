package com.ruslandontsov.fitness.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "SetEntry")
public class SetEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String exerciseName;

    private int reps;

    private double weight;

    private String muscleGroup;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workout workout;
}