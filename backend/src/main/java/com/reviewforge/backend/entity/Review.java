package com.reviewforge.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review {

    // ==========================================
    // ID
    // ==========================================

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    // ==========================================
    // REPOSITORY ID
    // ==========================================

    @Column(
            name = "repository_id",
            nullable = false
    )
    private Long repositoryId;


    // ==========================================
    // REVIEW TEXT
    // ==========================================

    @Column(
            name = "review_text",
            nullable = false,
            columnDefinition = "TEXT"
    )
    private String reviewText;


    // ==========================================
    // CREATED TIME
    // ==========================================

    @Column(
            name = "created_at"
    )
    private LocalDateTime createdAt;


    // ==========================================
    // AUTO SET CREATED TIME
    // ==========================================

    @PrePersist
    public void prePersist() {

        if (createdAt == null) {

            createdAt =
                    LocalDateTime.now();

        }
    }


    // ==========================================
    // GETTERS AND SETTERS
    // ==========================================

    public Long getId() {
        return id;
    }


    public Long getRepositoryId() {
        return repositoryId;
    }

    public void setRepositoryId(
            Long repositoryId
    ) {
        this.repositoryId =
                repositoryId;
    }


    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(
            String reviewText
    ) {
        this.reviewText =
                reviewText;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt =
                createdAt;
    }
}