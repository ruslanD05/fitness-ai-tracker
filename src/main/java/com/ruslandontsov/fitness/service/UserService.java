package com.ruslandontsov.fitness.service;

import com.ruslandontsov.fitness.model.User;
import com.ruslandontsov.fitness.repository.UserRepository;
import com.ruslandontsov.fitness.security.JwtService;
import com.ruslandontsov.fitness.security.MuscleRecoveryService;
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

    public User createUser(User user) {
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

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

}