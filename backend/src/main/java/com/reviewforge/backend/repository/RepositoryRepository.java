package com.reviewforge.backend.repository;

import com.reviewforge.backend.entity.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface RepositoryRepository
        extends JpaRepository<Repository, Long> {


    List<Repository> findByUserId(
            Long userId
    );


    Optional<Repository> findByUserIdAndRepoUrl(
            Long userId,
            String repoUrl
    );
}