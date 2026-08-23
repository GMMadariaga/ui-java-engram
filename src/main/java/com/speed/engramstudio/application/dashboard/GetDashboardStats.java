package com.speed.engramstudio.application.dashboard;

import com.speed.engramstudio.domain.model.DashboardStats;
import com.speed.engramstudio.domain.repository.DashboardRepository;
import com.speed.engramstudio.infrastructure.engram.dto.ObservationDto;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetDashboardStats {

    private final DashboardRepository dashboardRepository;

    public GetDashboardStats(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public CompletableFuture<DashboardStats> execute() {
        return dashboardRepository.getStats();
    }

    public CompletableFuture<List<ObservationDto>> getRecentObservations() {
        return dashboardRepository.getRecentObservations();
    }

    public CompletableFuture<List<SessionDto>> getRecentSessions() {
        return dashboardRepository.getRecentSessions();
    }
}