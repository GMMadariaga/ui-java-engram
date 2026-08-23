package com.speed.engramstudio.infrastructure.engram;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.model.Project;
import com.speed.engramstudio.domain.repository.ProjectRepository;
import com.speed.engramstudio.infrastructure.engram.api.ObservationsApi;
import com.speed.engramstudio.infrastructure.engram.api.SessionsApi;
import com.speed.engramstudio.infrastructure.engram.api.StatsApi;
import com.speed.engramstudio.infrastructure.engram.mapper.ObservationMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ProjectRepositoryImpl implements ProjectRepository {

    private final StatsApi statsApi;
    private final ObservationsApi observationsApi;
    private final SessionsApi sessionsApi;

    public ProjectRepositoryImpl(StatsApi statsApi, ObservationsApi observationsApi, SessionsApi sessionsApi) {
        this.statsApi = statsApi;
        this.observationsApi = observationsApi;
        this.sessionsApi = sessionsApi;
    }

    @Override
    public CompletableFuture<List<Project>> getAll() {
        return statsApi.getStats()
            .thenCombine(observationsApi.getAll(), (stats, observations) -> {
                Map<String, int[]> projectData = new LinkedHashMap<>();
                for (String p : stats.projects()) {
                    projectData.put(p, new int[]{0, 0});
                }
                for (var obs : observations) {
                    String project = obs.project();
                    if (project != null && !project.isBlank()) {
                        projectData.computeIfAbsent(project, k -> new int[]{0, 0})[0]++;
                    }
                }
                List<Project> result = new ArrayList<>();
                for (var entry : projectData.entrySet()) {
                    result.add(new Project(entry.getKey(), entry.getValue()[0], 0));
                }
                return result;
            });
    }

    @Override
    public CompletableFuture<List<Observation>> getObservationsByProject(String projectName) {
        return observationsApi.getAll()
            .thenApply(list -> {
                List<Observation> filtered = new ArrayList<>();
                for (var dto : list) {
                    if (projectName.equals(dto.project())) {
                        filtered.add(ObservationMapper.toDomain(dto));
                    }
                }
                return filtered;
            });
    }
}
