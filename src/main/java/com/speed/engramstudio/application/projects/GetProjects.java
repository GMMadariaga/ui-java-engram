package com.speed.engramstudio.application.projects;

import com.speed.engramstudio.domain.model.Project;
import com.speed.engramstudio.domain.repository.ProjectRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetProjects {

    private final ProjectRepository repository;

    public GetProjects(ProjectRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<List<Project>> execute() {
        return repository.getAll();
    }
}
