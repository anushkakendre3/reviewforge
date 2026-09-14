package com.reviewforge.backend.controller;

import com.reviewforge.backend.entity.Repository;
import com.reviewforge.backend.service.RepositoryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {

    private final RepositoryService repositoryService;

    public RepositoryController(RepositoryService repositoryService) {
        this.repositoryService = repositoryService;
    }

    @PostMapping
    public Repository createRepository(@RequestBody Repository repository) {
        return repositoryService.createRepository(repository);
    }

    @GetMapping("/user/{userId}")
    public List<Repository> getRepositoriesByUser(
            @PathVariable Long userId) {

        return repositoryService.getRepositoriesByUser(userId);
    }
}