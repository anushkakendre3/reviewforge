package com.reviewforge.backend.controller;

import com.reviewforge.backend.dto.SecurityCodeRequest;
import com.reviewforge.backend.service.JwtService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;


@RestController
public class SecurityCodeController {

    // SECURITY CODE

    private static final String SECURITY_CODE =
            "Vanaja";


    // JWT SERVICE

    private final JwtService jwtService;


    // CONSTRUCTOR

    public SecurityCodeController(
            JwtService jwtService
    ) {
        this.jwtService = jwtService;
    }


    // VERIFY SECURITY CODE

    @PostMapping("/verify-security-code")
    public ResponseEntity<?> verifySecurityCode(

            @RequestBody SecurityCodeRequest request,

            HttpServletRequest httpRequest
    ) {

        System.out.println(
                "SECURITY CODE ENDPOINT REACHED"
        );


        // VALIDATE SECURITY CODE

        if (
                request.getSecurityCode() == null
                ||
                request.getSecurityCode().isBlank()
        ) {

            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(
                            createError(
                                    "Security code is required"
                            )
                    );
        }


        // CHECK SECURITY CODE

        if (
                !SECURITY_CODE.equals(
                        request
                                .getSecurityCode()
                                .trim()
                )
        ) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            createError(
                                    "Invalid security code"
                            )
                    );
        }


        // GET AUTHORIZATION HEADER

        String authorizationHeader =
                httpRequest.getHeader(
                        "Authorization"
                );


        if (
                authorizationHeader == null
                ||
                !authorizationHeader.startsWith(
                        "Bearer "
                )
        ) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            createError(
                                    "Authentication token is required"
                            )
                    );
        }


        // EXTRACT TOKEN

        String token =
                authorizationHeader.substring(7);


        try {

            // EXTRACT EMAIL FROM OLD TOKEN

            String email =
                    jwtService.extractEmail(
                            token
                    );


            // GENERATE NEW VERIFIED TOKEN

            String verifiedToken =
                    jwtService.generateVerifiedToken(
                            email
                    );


            System.out.println(
                    "Security code verified for: "
                            + email
            );


            // SUCCESS RESPONSE

            Map<String, Object> response =
                    new HashMap<>();


            response.put(
                    "success",
                    true
            );


            response.put(
                    "message",
                    "Security code verified successfully"
            );


            response.put(
                    "token",
                    verifiedToken
            );


            return ResponseEntity.ok(
                    response
            );

        } catch (Exception error) {

            System.out.println(
                    "Security code verification error: "
                            + error.getMessage()
            );


            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            createError(
                                    "Invalid or expired authentication token"
                            )
                    );
        }
    }


    // CREATE ERROR RESPONSE

    private Map<String, Object> createError(
            String message
    ) {

        Map<String, Object> error =
                new HashMap<>();


        error.put(
                "success",
                false
        );


        error.put(
                "message",
                message
        );


        return error;
    }
}