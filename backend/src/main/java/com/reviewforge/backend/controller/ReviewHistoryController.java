package com.reviewforge.backend.controller;

import com.reviewforge.backend.entity.Repository;
import com.reviewforge.backend.entity.Review;
import com.reviewforge.backend.entity.User;

import com.reviewforge.backend.service.JwtService;
import com.reviewforge.backend.service.RepositoryService;
import com.reviewforge.backend.service.ReviewService;
import com.reviewforge.backend.service.UserService;

import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api")
public class ReviewHistoryController {

    // SERVICES

    private final JwtService jwtService;
    private final UserService userService;
    private final RepositoryService repositoryService;
    private final ReviewService reviewService;


    // CONSTRUCTOR

    public ReviewHistoryController(
            JwtService jwtService,
            UserService userService,
            RepositoryService repositoryService,
            ReviewService reviewService
    ) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.repositoryService = repositoryService;
        this.reviewService = reviewService;
    }


    // GET REVIEW HISTORY

    @GetMapping("/history")
    public ResponseEntity<?> getReviewHistory(
            @RequestHeader("Authorization") String authHeader
    ) {

        try {

            // CLEAN TOKEN

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


            // GET EMAIL FROM JWT

            String email =
                    jwtService.extractEmail(
                            token
                    );


            // FIND USER

            User user =
                    userService.findByEmail(
                            email
                    );


            if (user == null) {

                return ResponseEntity
                        .status(401)
                        .body(
                                createError(
                                        "User not found."
                                )
                        );
            }


            // GET USER REPOSITORIES

            List<Repository> repositories =
                    repositoryService
                            .getRepositoriesByUser(
                                    user.getId()
                            );


            // HISTORY RESPONSE

            List<Map<String, Object>> history =
                    new ArrayList<>();


            // PROCESS EACH REPOSITORY

            for (Repository repository : repositories) {

                Map<String, Object> repositoryData =
                        new HashMap<>();


                repositoryData.put(
                        "repositoryId",
                        repository.getId()
                );


                repositoryData.put(
                        "repositoryName",
                        repository.getRepoName()
                );


                repositoryData.put(
                        "repositoryUrl",
                        repository.getRepoUrl()
                );


                repositoryData.put(
                        "createdAt",
                        repository.getCreatedAt()
                );


                // GET REVIEWS

                List<Review> reviews =
                        reviewService
                                .getReviewsByRepository(
                                        repository.getId()
                                );


                List<Map<String, Object>> reviewList =
                        new ArrayList<>();


                // PROCESS REVIEWS

                for (Review review : reviews) {

                    Map<String, Object> reviewData =
                            new HashMap<>();


                    reviewData.put(
                            "reviewId",
                            review.getId()
                    );


                    reviewData.put(
                            "reviewText",
                            review.getReviewText()
                    );


                    reviewData.put(
                            "createdAt",
                            review.getCreatedAt()
                    );


                    reviewList.add(
                            reviewData
                    );
                }


                // ADD REVIEWS

                repositoryData.put(
                        "reviews",
                        reviewList
                );


                // ADD REPOSITORY

                history.add(
                        repositoryData
                );
            }


            // RETURN HISTORY

            return ResponseEntity.ok(
                    history
            );

        } catch (Exception error) {

            error.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            createError(
                                    "Unable to load review history."
                            )
                    );
        }
    }


    // CREATE ERROR RESPONSE

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