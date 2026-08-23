package com.speed.engramstudio.domain.repository;

import com.speed.engramstudio.domain.model.Project;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ProjectRepository {
    CompletableFuture<List<Project>> getAll();
    CompletableFuture<List<com.speed.engramstudio.domain.model.Observation>> getObservationsByProject(String projectName);
}
