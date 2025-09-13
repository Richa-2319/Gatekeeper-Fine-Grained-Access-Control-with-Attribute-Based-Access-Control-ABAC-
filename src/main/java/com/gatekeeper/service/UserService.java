// Update: src/main/java/com/gatekeeper/service/UserService.java
package com.gatekeeper.service;

import com.gatekeeper.model.User;
import com.gatekeeper.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public User createUser(User user) {
        // Check if user already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists: " + user.getUsername());
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists: " + user.getEmail());
        }

        // Encode password and save user
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        log.info("User created successfully: {}", savedUser.getUsername());
        return savedUser;
    }

    public Optional<User> authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();

            // Check if user is active
            if (!user.isActive()) {
                log.warn("Inactive user attempted login: {}", username);
                return Optional.empty();
            }

            // Verify password
            if (passwordEncoder.matches(password, user.getPassword())) {
                // Update last login
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);

                log.info("User authenticated successfully: {}", username);
                return Optional.of(user);
            }
        }

        log.warn("Authentication failed for user: {}", username);
        return Optional.empty();
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
