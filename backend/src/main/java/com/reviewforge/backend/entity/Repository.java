package com.reviewforge.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "repositories")
public class Repository {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;


    // ==========================================
    // USER ID
    // ==========================================

    @Column(
            name = "user_id",
            nullable = false
    )
    private Long userId;


    // ==========================================
    // REPOSITORY URL
    // ==========================================

    @Column(
            name = "repo_url",
            nullable = false,
            length = 500
    )
    private String repoUrl;


    // ==========================================
    // REPOSITORY NAME
    // ==========================================

    @Column(
            name = "repo_name",
            nullable = false
    )
    private String repoName;


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


    public Long getUserId() {
        return userId;
    }

    public void setUserId(
            Long userId
    ) {
        this.userId = userId;
    }


    public String getRepoUrl() {
        return repoUrl;
    }

    public void setRepoUrl(
            String repoUrl
    ) {
        this.repoUrl = repoUrl;
    }


    public String getRepoName() {
        return repoName;
    }

    public void setRepoName(
            String repoName
    ) {
        this.repoName = repoName;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            LocalDateTime createdAt
    ) {
        this.createdAt = createdAt;
    }
}