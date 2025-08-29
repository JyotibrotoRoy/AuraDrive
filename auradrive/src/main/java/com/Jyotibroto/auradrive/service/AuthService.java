package com.Jyotibroto.auradrive.service;

import com.Jyotibroto.auradrive.entity.User;
import com.Jyotibroto.auradrive.enums.ROLES;
import com.Jyotibroto.auradrive.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthService {
    @Autowired
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public User registerNewUser(User user) {
        if(userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("Error: A user with this email already exists.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(ROLES.RIDER);
        try {
            User savedUser = userRepository.save(user);
            log.info("Successfully registered new user with email id: {}", savedUser.getEmail());
            return savedUser;
        }catch (DataIntegrityViolationException e) {
            log.error("Database error while registering new user with email id: {}. error: {}", user.getEmail(), e.getMessage());
            throw new IllegalStateException("Error: this phone number may already be in use");
        }
    }
}
