package com.reviewforge.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.reviewforge.backend.dto.AiReviewRequest;

import com.reviewforge.backend.entity.Repository;
import com.reviewforge.backend.entity.Review;
import com.reviewforge.backend.entity.User;

import com.reviewforge.backend.service.JwtService;
import com.reviewforge.backend.service.RepositoryService;
import com.reviewforge.backend.service.ReviewService;
import com.reviewforge.backend.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URLEncoder;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.charset.StandardCharsets;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class AiReviewController {


    // ==========================================
    // AI SERVICE URL
    // ==========================================

    private static final String AI_SERVICE_URL =
            "http://127.0.0.1:8000";


    // ==========================================
    // SERVICES
    // ==========================================

    private final JwtService jwtService;

    private final UserService userService;

    private final RepositoryService repositoryService;

    private final ReviewService reviewService;


    // ==========================================
    // HTTP CLIENT
    // ==========================================

    private final HttpClient httpClient;


    // ==========================================
    // JSON MAPPER
    // ==========================================

    private final ObjectMapper objectMapper;


    // ==========================================
    // CONSTRUCTOR
    // ==========================================

    public AiReviewController(

            JwtService jwtService,

            UserService userService,

            RepositoryService repositoryService,

            ReviewService reviewService,

            ObjectMapper objectMapper

    ) {

        this.jwtService =
                jwtService;

        this.userService =
                userService;

        this.repositoryService =
                repositoryService;

        this.reviewService =
                reviewService;

        this.httpClient =
                HttpClient.newHttpClient();

        this.objectMapper =
                objectMapper;
    }


    // ==========================================
    // REVIEW REPOSITORY
    // ==========================================

    @PostMapping("/review")
    public ResponseEntity<?> reviewRepository(

            @RequestBody
            AiReviewRequest request,

            @RequestHeader("Authorization")
            String authHeader

    ) {


        System.out.println(
                "\n================================="
        );

        System.out.println(
                "AI REVIEW REQUEST RECEIVED"
        );

        System.out.println(
                "================================="
        );


        // ======================================
        // VALIDATE REQUEST
        // ======================================

        if (

                request == null

                ||

                request.getRepoUrl() == null

                ||

                request.getRepoUrl().isBlank()

        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            createError(
                                    "GitHub repository URL is required."
                            )
                    );
        }


        String repoUrl =
                request
                        .getRepoUrl()
                        .trim();


        // ======================================
        // VALIDATE GITHUB URL
        // ======================================

        if (

                !repoUrl.startsWith(
                        "https://github.com/"
                )

        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            createError(
                                    "Please enter a valid GitHub repository URL."
                            )
                    );
        }


        try {


            // ==================================
            // CLEAN JWT TOKEN
            // ==================================

            String token =
                    authHeader

                            .replaceFirst(
                                    "(?i)^Bearer\\s+",
                                    ""
                            )

                            .replaceAll(
                                    "\\s+",
                                    ""
                            )

                            .trim();


            // ==================================
            // EXTRACT EMAIL FROM JWT
            // ==================================

            String email =
                    jwtService.extractEmail(
                            token
                    );


            System.out.println(
                    "Logged in user email: "
                            + email
            );


            // ==================================
            // FIND USER
            // ==================================

            User user =
                    userService.findByEmail(
                            email
                    );


            if (user == null) {

                return ResponseEntity
                        .status(
                                HttpStatus.UNAUTHORIZED
                        )
                        .body(
                                createError(
                                        "User not found."
                                )
                        );
            }


            System.out.println(
                    "User found: "
                            + user.getId()
            );


            // ==================================
            // EXTRACT REPOSITORY NAME
            // ==================================

            String repoName =
                    extractRepositoryName(
                            repoUrl
                    );


            System.out.println(
                    "Repository name: "
                            + repoName
            );


            // ==================================
            // ENCODE REPOSITORY URL
            // ==================================

            String encodedRepoUrl =
                    URLEncoder.encode(

                            repoUrl,

                            StandardCharsets.UTF_8

                    );


            // ==================================
            // CREATE FASTAPI URL
            // ==================================

            String requestUrl =
                    AI_SERVICE_URL
                            + "/review/github"
                            + "?repo_url="
                            + encodedRepoUrl;


            System.out.println(
                    "Calling AI Service:"
            );

            System.out.println(
                    requestUrl
            );


            // ==================================
            // CREATE HTTP REQUEST
            // ==================================

            HttpRequest httpRequest =
                    HttpRequest.newBuilder()

                            .uri(
                                    URI.create(
                                            requestUrl
                                    )
                            )

                            .POST(
                                    HttpRequest
                                            .BodyPublishers
                                            .noBody()
                            )

                            .header(
                                    "Accept",
                                    "application/json"
                            )

                            .build();


            // ==================================
            // CALL FASTAPI
            // ==================================

            HttpResponse<String> response =
                    httpClient.send(

                            httpRequest,

                            HttpResponse
                                    .BodyHandlers
                                    .ofString()

                    );


            int statusCode =
                    response.statusCode();


            String responseBody =
                    response.body();


            System.out.println(
                    "AI Service Status: "
                            + statusCode
            );


            // ==================================
            // HANDLE AI SERVICE ERROR
            // ==================================

            if (statusCode >= 400) {


                System.out.println(
                        "AI Service Error:"
                );

                System.out.println(
                        responseBody
                );


                Map<String, Object> errorResponse =
                        new HashMap<>();


                errorResponse.put(

                        "error",

                        "AI service failed to analyze the repository."

                );


                errorResponse.put(

                        "details",

                        responseBody

                );


                return ResponseEntity

                        .status(
                                statusCode
                        )

                        .body(
                                errorResponse
                        );
            }


            // ==================================
            // PARSE FASTAPI RESPONSE
            // ==================================

            Map<String, Object> aiResponse =
                    objectMapper.readValue(

                            responseBody,

                            new TypeReference<
                                    Map<String, Object>
                                    >() {
                            }

                    );


            // ==================================
            // GET REVIEW TEXT
            // ==================================

            Object reviewObject =
                    aiResponse.get(
                            "review"
                    );


            String reviewText;


            if (reviewObject == null) {

                reviewText =
                        "No review was generated.";

            }

            else if (

                    reviewObject
                            instanceof String

            ) {

                reviewText =
                        (String)
                                reviewObject;

            }

            else {

                reviewText =
                        objectMapper
                                .writeValueAsString(
                                        reviewObject
                                );
            }


            // ==================================
            // GET FILES
            // ==================================

            List<String> files = null;


            Object filesObject =
                    aiResponse.get(
                            "files"
                    );


            if (

                    filesObject
                            instanceof List

            ) {

                try {

                    files =
                            objectMapper.convertValue(

                                    filesObject,

                                    new TypeReference<
                                            List<String>
                                            >() {
                                    }

                            );

                } catch (Exception error) {

                    System.out.println(
                            "Unable to parse file list."
                    );
                }
            }


            // ==================================
            // GET CHUNK COUNT
            // ==================================

            int chunkCount =
                    getChunkCount(
                            aiResponse
                    );


                // ==================================
                // GET OR CREATE REPOSITORY
                // ==================================

                Repository savedRepository =
                        repositoryService
                                .getOrCreateRepository(

                                        user.getId(),

                                        repoUrl,

                                        repoName

                                );


                System.out.println(
                        "Using Repository ID: "
                                + savedRepository.getId()
                );
            // ==================================
            // SAVE REVIEW
            // ==================================

            Review review =
                    new Review();


            review.setRepositoryId(
                    savedRepository.getId()
            );


            review.setReviewText(
                    reviewText
            );


            Review savedReview =
                    reviewService
                            .createReview(
                                    review
                            );


            System.out.println(
                    "Review saved with ID: "
                            + savedReview.getId()
            );


            // ==================================
            // ADD DATABASE INFORMATION
            // ==================================

            aiResponse.put(

                    "repository_id",

                    savedRepository.getId()

            );


            aiResponse.put(

                    "review_id",

                    savedReview.getId()

            );


            aiResponse.put(

                    "repo_name",

                    repoName

            );


            if (files != null) {

                aiResponse.put(

                        "files",

                        files

                );
            }


            aiResponse.put(

                    "chunk_count",

                    chunkCount

            );


            // ==================================
            // DEBUG
            // ==================================

            System.out.println(
                    "AI review completed successfully."
            );


            System.out.println(
                    "Repository: "
                            + repoName
            );


            System.out.println(
                    "Files analyzed: "
                            + (
                            files != null
                                    ? files.size()
                                    : 0
                    )
            );


            System.out.println(
                    "Chunks analyzed: "
                            + chunkCount
            );


            // ==================================
            // RETURN TO FRONTEND
            // ==================================

            return ResponseEntity.ok(
                    aiResponse
            );


        }


        // ======================================
        // ERROR HANDLING
        // ======================================

        catch (Exception error) {


            System.out.println(
                    "\n================================="
            );

            System.out.println(
                    "AI REVIEW ERROR"
            );

            System.out.println(
                    "================================="
            );


            error.printStackTrace();


            Map<String, Object> errorResponse =
                    new HashMap<>();


            errorResponse.put(

                    "error",

                    "Unable to analyze repository."

            );


            errorResponse.put(

                    "details",

                    error.getMessage()

            );


            return ResponseEntity

                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )

                    .body(
                            errorResponse
                    );
        }
    }


    // ==========================================
    // EXTRACT REPOSITORY NAME
    // ==========================================

    private String extractRepositoryName(
            String repoUrl
    ) {


        String cleanUrl =
                repoUrl
                        .replaceAll(
                                "/+$",
                                ""
                        );


        int lastSlash =
                cleanUrl.lastIndexOf(
                        "/"
                );


        if (

                lastSlash == -1

                ||

                lastSlash
                        ==
                        cleanUrl.length() - 1

        ) {

            return "Unknown Repository";
        }


        return cleanUrl.substring(
                lastSlash + 1
        );
    }


    // ==========================================
    // GET CHUNK COUNT
    // ==========================================

    private int getChunkCount(
            Map<String, Object> aiResponse
    ) {


        Object chunkObject =
                aiResponse.get(
                        "chunk_count"
                );


        if (

                chunkObject
                        instanceof Number

        ) {

            return (
                    (Number)
                            chunkObject
            ).intValue();
        }


        if (

                chunkObject != null

        ) {

            try {

                return Integer.parseInt(

                        chunkObject.toString()

                );

            }

            catch (Exception ignored) {

                return 0;
            }
        }


        return 0;
    }


    // ==========================================
    // CREATE ERROR RESPONSE
    // ==========================================

    private Map<String, String> createError(
            String message
    ) {


        Map<String, String> error =
                new HashMap<>();


        error.put(

                "error",

                message

        );


        return error;
    }
}