package com.reviewforge.backend.service;

import com.reviewforge.backend.entity.Review;
import com.reviewforge.backend.repository.ReviewRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ReviewService {


    private final ReviewRepository reviewRepository;


    public ReviewService(
            ReviewRepository reviewRepository
    ) {

        this.reviewRepository = reviewRepository;

    }


    // ==========================================
    // CREATE REVIEW
    // ==========================================

    public Review createReview(
            Review review
    ) {


        if (
                review.getCreatedAt() == null
        ) {

            review.setCreatedAt(
                    LocalDateTime.now()
            );

        }


        return reviewRepository.save(
                review
        );

    }


    // ==========================================
    // GET REVIEWS BY REPOSITORY
    // ==========================================

    public List<Review> getReviewsByRepository(
            Long repositoryId
    ) {

        return reviewRepository
                .findByRepositoryId(
                        repositoryId
                );

    }

}