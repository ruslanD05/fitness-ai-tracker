package com.ruslandontsov.fitness.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String name;

    private String password;

    @Enumerated(EnumType.STRING)
    private UserGoal goal;

    @Enumerated(EnumType.STRING)
    private UserExperience experienceLevel;
}