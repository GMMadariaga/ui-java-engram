package com.speed.engramstudio.domain.repository;

import com.speed.engramstudio.domain.model.DashboardStats;
import com.speed.engramstudio.infrastructure.engram.dto.ObservationDto;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DashboardRepository {
    CompletableFuture<DashboardStats> getStats();
    CompletableFuture<List<ObservationDto>> getRecentObservations();
    CompletableFuture<List<SessionDto>> getRecentSessions();
}