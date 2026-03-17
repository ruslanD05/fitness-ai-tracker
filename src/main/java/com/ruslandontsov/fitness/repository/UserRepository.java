package com.ruslandontsov.fitness.repository;

import com.ruslandontsov.fitness.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
