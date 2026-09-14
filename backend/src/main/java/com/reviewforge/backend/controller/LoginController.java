package com.reviewforge.backend.controller;

import com.reviewforge.backend.dto.LoginRequest;
import com.reviewforge.backend.entity.User;
import com.reviewforge.backend.service.JwtService;
import com.reviewforge.backend.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class LoginController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;


    public LoginController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }


    // LOGIN

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest request
    ) {

        // FIND USER

        User user = userService.findByEmail(
                request.getEmail().trim()
        );


        if (user == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new ErrorResponse(
                                    "User not found"
                            )
                    );
        }


        // CHECK PASSWORD

        boolean passwordMatches =
                passwordEncoder.matches(
                        request.getPassword(),
                        user.getPassword()
                );


        if (!passwordMatches) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new ErrorResponse(
                                    "Invalid password"
                            )
                    );
        }


        // GENERATE JWT

        String token =
                jwtService.generateToken(
                        user.getEmail().trim()
                );


        System.out.println(
                "Login successful for: "
                        + user.getEmail()
        );


        System.out.println(
                "JWT generated. Length: "
                        + token.length()
        );


        // RETURN TOKEN

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }


    // LOGIN RESPONSE

    public static class LoginResponse {

        private String token;


        public LoginResponse(String token) {
            this.token = token;
        }


        public String getToken() {
            return token;
        }
    }


    // ERROR RESPONSE

    public static class ErrorResponse {

        private String error;


        public ErrorResponse(String error) {
            this.error = error;
        }


        public String getError() {
            return error;
        }
    }
}