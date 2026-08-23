package com.speed.engramstudio.infrastructure.engram;

import com.speed.engramstudio.domain.model.DashboardStats;
import com.speed.engramstudio.domain.repository.DashboardRepository;
import com.speed.engramstudio.infrastructure.engram.api.ObservationsApi;
import com.speed.engramstudio.infrastructure.engram.api.SessionsApi;
import com.speed.engramstudio.infrastructure.engram.api.StatsApi;
import com.speed.engramstudio.infrastructure.engram.dto.ObservationDto;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DashboardRepositoryImpl implements DashboardRepository {

    private final StatsApi statsApi;
    private final ObservationsApi observationsApi;
    private final SessionsApi sessionsApi;

    public DashboardRepositoryImpl(StatsApi statsApi, ObservationsApi observationsApi, SessionsApi sessionsApi) {
        this.statsApi = statsApi;
        this.observationsApi = observationsApi;
        this.sessionsApi = sessionsApi;
    }

    @Override
    public CompletableFuture<DashboardStats> getStats() {
        return statsApi.getStats();
    }

    @Override
    public CompletableFuture<List<ObservationDto>> getRecentObservations() {
        return observationsApi.getRecent();
    }

    @Override
    public CompletableFuture<List<SessionDto>> getRecentSessions() {
        return sessionsApi.getRecent();
    }
}