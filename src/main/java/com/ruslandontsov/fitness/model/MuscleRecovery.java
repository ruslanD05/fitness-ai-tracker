package com.ruslandontsov.fitness.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "muscle_recovery")
@Getter
@Setter
public class MuscleRecovery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    private MuscleGroup muscleGroup;
    private double fatigueScore;

    private LocalDate lastUpdatedDate;
}