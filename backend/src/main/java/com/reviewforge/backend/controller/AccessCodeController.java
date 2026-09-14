package com.reviewforge.backend.controller;

import com.reviewforge.backend.dto.AccessCodeRequest;
import com.reviewforge.backend.service.JwtService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessCodeController {

    private final JwtService jwtService;

    // SECURITY CODE

    private static final String ACCESS_CODE =
            "RF-ACCESS-2026";

    public AccessCodeController(
            JwtService jwtService
    ) {

        this.jwtService = jwtService;
    }

    // VERIFY ACCESS CODE

    @PostMapping("/access/verify")
    public ResponseEntity<?> verifyAccessCode(

            @RequestHeader(
                    value = "Authorization",
                    required = false
            )
            String authorizationHeader,

            @RequestBody
            AccessCodeRequest request
    ) {

        try {

            // CHECK JWT EXISTS

            if (
                    authorizationHeader == null
                    || authorizationHeader.isBlank()
            ) {

                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(
                                new ErrorResponse(
                                        "Login required"
                                )
                        );
            }

            // EXTRACT USER EMAIL

            String email =
                    jwtService.extractEmail(
                            authorizationHeader
                    );

            // CHECK ALREADY VERIFIED

            boolean alreadyVerified =
                    jwtService.isAccessVerified(
                            authorizationHeader
                    );

            if (alreadyVerified) {

                return ResponseEntity.ok(
                        new AccessCodeResponse(

                                "Access already verified",

                                authorizationHeader
                                        .replaceFirst(
                                                "(?i)^Bearer\\s+",
                                                ""
                                        )
                        )
                );
            }

            // CHECK REQUEST

            if (
                    request == null
                    || request.getCode() == null
                    || request.getCode().isBlank()
            ) {

                return ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(
                                new ErrorResponse(
                                        "Security code is required"
                                )
                        );
            }

            // VERIFY SECURITY CODE

            boolean validCode =
                    ACCESS_CODE.equals(
                            request.getCode().trim()
                    );

            if (!validCode) {

                return ResponseEntity
                        .status(HttpStatus.FORBIDDEN)
                        .body(
                                new ErrorResponse(
                                        "Invalid security code"
                                )
                        );
            }

            // GENERATE VERIFIED JWT

            String verifiedToken =
                    jwtService.generateVerifiedToken(
                            email
                    );

            System.out.println(
                    "Security code verified for: "
                            + email
            );

            // RETURN VERIFIED TOKEN

            return ResponseEntity.ok(
                    new AccessCodeResponse(

                            "Security code verified successfully",

                            verifiedToken
                    )
            );

        } catch (Exception error) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            new ErrorResponse(
                                    "Invalid or expired login token"
                            )
                    );
        }
    }

    // SUCCESS RESPONSE

    public static class AccessCodeResponse {

        private String message;

        private String token;

        public AccessCodeResponse(
                String message,
                String token
        ) {

            this.message = message;

            this.token = token;
        }

        public String getMessage() {

            return message;
        }

        public String getToken() {

            return token;
        }
    }

    // ERROR RESPONSE

    public static class ErrorResponse {

        private String error;

        public ErrorResponse(
                String error
        ) {

            this.error = error;
        }

        public String getError() {

            return error;
        }
    }
}