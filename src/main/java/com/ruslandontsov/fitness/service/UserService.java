package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.model.UserExperience;
import com.ruslandontsov.fitness.model.UserGoal;
import com.ruslandontsov.fitness.repository.UserRepository;
import com.ruslandontsov.fitness.security.JwtService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final MuscleRecoveryService muscleRecoveryService;

    public UserService(UserRepository userRepository,  JwtService jwtService,  MuscleRecoveryService muscleRecoveryService) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.muscleRecoveryService = muscleRecoveryService;
    }

    private User createUser(User user) {
        return userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public boolean existsByEmail(String email) { return userRepository.existsByEmail(email); }

    public User register(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));;
        User saved = userRepository.save(user);

        //default values, since they are not asked during registration
        user.setExperienceLevel(UserExperience.NO_EXPERIENCE);
        user.setGoal(UserGoal.MUSCLE_GROWTH);

        muscleRecoveryService.initializeRecoveryForUser(saved);
        return saved;
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if (!BCrypt.checkpw(password, user.getPassword())) {
            throw new RuntimeException("Wrong password");
        }
        return jwtService.generateToken(user.getId());
    }


    public Long getCurrentUserId() {
        return (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    private void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void updateUserExperience(UserExperience userExperience){
        User user = getUserById(getCurrentUserId()).orElseThrow();
        user.setExperienceLevel(userExperience);
        userRepository.save(user);
    }
    public void updateUserGoal(UserGoal userGoal){
        User user = getUserById(getCurrentUserId()).orElseThrow();
        user.setGoal(userGoal);
        userRepository.save(user);
    }

}