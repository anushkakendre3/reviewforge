package com.reviewforge.backend.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reviewforge.backend.entity.User;
import com.reviewforge.backend.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    // ==========================================
    // CREATE USER
    // ==========================================

    public User createUser(User user) {

        if (user == null) {

            throw new IllegalArgumentException(
                    "User data is required"
            );
        }


        if (user.getEmail() == null
                || user.getEmail().isBlank()) {

            throw new IllegalArgumentException(
                    "Email is required"
            );
        }


        if (user.getPassword() == null
                || user.getPassword().isBlank()) {

            throw new IllegalArgumentException(
                    "Password is required"
            );
        }


        String cleanEmail =
                user.getEmail()
                        .trim()
                        .toLowerCase();


        if (userRepository
                .findByEmail(cleanEmail)
                .isPresent()) {

            throw new IllegalArgumentException(
                    "Email already registered"
            );
        }


        user.setEmail(
                cleanEmail
        );


        user.setPassword(
                passwordEncoder.encode(
                        user.getPassword()
                )
        );


        return userRepository.save(
                user
        );
    }


    // ==========================================
    // FIND USER BY EMAIL
    // ==========================================

    public User findByEmail(
            String email
    ) {

        if (email == null
                || email.isBlank()) {

            return null;
        }


        return userRepository

                .findByEmail(

                        email
                                .trim()
                                .toLowerCase()

                )

                .orElse(null);
    }


    // ==========================================
    // GET USER BY ID
    // ==========================================

    public User findById(
            Long id
    ) {

        if (id == null) {

            return null;
        }


        return userRepository

                .findById(id)

                .orElse(null);
    }
}