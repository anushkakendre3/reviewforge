package com.reviewforge.backend.controller;

import com.reviewforge.backend.dto.AiReviewRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.client.RestTemplate;

import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class ReviewController {


    private final RestTemplate restTemplate;


    public ReviewController(
            RestTemplate restTemplate
    ) {

        this.restTemplate = restTemplate;

    }


    // ==========================================
    // REVIEW GITHUB REPOSITORY
    // ==========================================

    @PostMapping("/review")
    public ResponseEntity<?> reviewRepository(

            @RequestBody AiReviewRequest request,

            Authentication authentication

    ) {


        // ======================================
        // CHECK AUTHENTICATION
        // ======================================

        if (authentication == null) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(
                            Map.of(
                                    "error",
                                    "Please login first."
                            )
                    );

        }


        // ======================================
        // CHECK REQUEST
        // ======================================

        if (request == null) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Request data is required."
                            )
                    );

        }


        // ======================================
        // GET REPOSITORY URL
        // ======================================

        String repoUrl =
                request.getRepoUrl();


        if (
                repoUrl == null
                        ||
                repoUrl.trim().isEmpty()
        ) {

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "error",
                                    "Repository URL is required."
                            )
                    );

        }


        repoUrl = repoUrl.trim();


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
                            Map.of(
                                    "error",
                                    "Please enter a valid GitHub repository URL."
                            )
                    );

        }


        try {


            // ==================================
            // BUILD FASTAPI URL
            // ==================================

            String aiServiceUrl =

                    UriComponentsBuilder

                            .fromUriString(
                                    "http://127.0.0.1:8000/review/github"
                            )

                            .queryParam(
                                    "repo_url",
                                    repoUrl
                            )

                            .build()

                            .encode()

                            .toUriString();


            // ==================================
            // CALL AI SERVICE
            // ==================================

            ResponseEntity<Map> aiResponse =

                    restTemplate.getForEntity(

                            aiServiceUrl,

                            Map.class

                    );


            // ==================================
            // GET AI DATA
            // ==================================

            Map<String, Object> aiData =

                    aiResponse.getBody();


            // ==================================
            // CHECK AI RESPONSE
            // ==================================

            if (aiData == null) {

                return ResponseEntity

                        .status(
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )

                        .body(

                                Map.of(

                                        "error",

                                        "AI service returned no data."

                                )

                        );

            }


            // ==================================
            // CREATE CLEAN RESPONSE
            // ==================================

            Map<String, Object> response =

                    new HashMap<>();


            // ==================================
            // STATUS
            // ==================================

            response.put(

                    "status",

                    aiData.getOrDefault(

                            "status",

                            "completed"

                    )

            );


            // ==================================
            // REPOSITORY NAME
            // ==================================

            response.put(

                    "repo_name",

                    aiData.getOrDefault(

                            "repo_name",

                            "Unknown Repository"

                    )

            );


            // ==================================
            // FILES
            // ==================================

            response.put(

                    "files",

                    aiData.getOrDefault(

                            "files",

                            List.of()

                    )

            );


            // ==================================
            // FILES ANALYZED
            // ==================================

            response.put(

                    "files_analyzed",

                    aiData.getOrDefault(

                            "files_analyzed",

                            0

                    )

            );


            // ==================================
            // TOTAL CHUNKS
            // ==================================

            response.put(

                    "chunk_count",

                    aiData.getOrDefault(

                            "chunk_count",

                            0

                    )

            );


            // ==================================
            // RETRIEVED CHUNKS
            // ==================================

            response.put(

                    "retrieved_chunks",

                    aiData.getOrDefault(

                            "retrieved_chunks",

                            0

                    )

            );


            // ==================================
            // REVIEW TEXT
            // ==================================

            response.put(

                    "review",

                    aiData.getOrDefault(

                            "review",

                            "No review generated."

                    )

            );


            // ==================================
            // AI STATUS
            // ==================================

            response.put(

                    "ai_used",

                    aiData.getOrDefault(

                            "ai_used",

                            false

                    )

            );


            // ==================================
            // REVIEW ENGINE
            // ==================================

            response.put(

                    "review_engine",

                    aiData.getOrDefault(

                            "review_engine",

                            "Unknown"

                    )

            );


            // ==================================
            // ANALYSIS TYPE
            // ==================================

            response.put(

                    "analysis_type",

                    aiData.getOrDefault(

                            "analysis_type",

                            "Static Analysis"

                    )

            );


            // ==================================
            // SUCCESS MESSAGE
            // ==================================

            response.put(

                    "message",

                    "Repository analyzed successfully."

            );


            return ResponseEntity.ok(
                    response
            );


        }


        // ======================================
        // ERROR
        // ======================================

        catch (Exception error) {


            error.printStackTrace();


            return ResponseEntity

                    .status(
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )

                    .body(

                            Map.of(

                                    "error",

                                    "Unable to analyze repository.",

                                    "details",

                                    error.getMessage() == null

                                            ?

                                            "Unknown error"

                                            :

                                            error.getMessage()

                            )

                    );

        }

    }

}