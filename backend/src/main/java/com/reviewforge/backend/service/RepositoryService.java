package com.reviewforge.backend.service;

import com.reviewforge.backend.entity.Repository;
import com.reviewforge.backend.repository.RepositoryRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Service
public class RepositoryService {

    private final RepositoryRepository repositoryRepository;


    public RepositoryService(
            RepositoryRepository repositoryRepository
    ) {

        this.repositoryRepository =
                repositoryRepository;
    }


    // CREATE REPOSITORY

    public Repository createRepository(
            Repository repository
    ) {

        if (
                repository.getCreatedAt() == null
        ) {

            repository.setCreatedAt(
                    LocalDateTime.now()
            );
        }


        return repositoryRepository.save(
                repository
        );
    }


    // FIND REPOSITORY

    public Repository findByUserAndRepoUrl(

            Long userId,

            String repoUrl

    ) {

        Optional<Repository> repository =
                repositoryRepository
                        .findByUserIdAndRepoUrl(
                                userId,
                                repoUrl
                        );


        return repository.orElse(
                null
        );
    }


    // GET OR CREATE REPOSITORY

    public Repository getOrCreateRepository(

            Long userId,

            String repoUrl,

            String repoName

    ) {

        // CHECK EXISTING REPOSITORY

        Repository existingRepository =
                findByUserAndRepoUrl(

                        userId,

                        repoUrl

                );


        // REPOSITORY EXISTS

        if (
                existingRepository != null
        ) {

            System.out.println(
                    "Repository already exists."
            );

            System.out.println(
                    "Repository ID: "
                            + existingRepository.getId()
            );


            return existingRepository;
        }


        // CREATE NEW REPOSITORY

        Repository newRepository =
                new Repository();


        newRepository.setUserId(
                userId
        );


        newRepository.setRepoUrl(
                repoUrl
        );


        newRepository.setRepoName(
                repoName
        );


        newRepository.setCreatedAt(
                LocalDateTime.now()
        );


        Repository savedRepository =
                repositoryRepository.save(
                        newRepository
                );


        System.out.println(
                "New repository created."
        );

        System.out.println(
                "Repository ID: "
                        + savedRepository.getId()
        );


        return savedRepository;
    }


    // GET USER REPOSITORIES

    public List<Repository> getRepositoriesByUser(
            Long userId
    ) {

        return repositoryRepository
                .findByUserId(
                        userId
                );
    }
}