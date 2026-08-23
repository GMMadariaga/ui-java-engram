package com.speed.engramstudio.application.projects;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.repository.ProjectRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetProjectObservations {

    private final ProjectRepository repository;

    public GetProjectObservations(ProjectRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<List<Observation>> execute(String projectName) {
        return repository.getObservationsByProject(projectName);
    }
}
