package com.speed.engramstudio.infrastructure.engram.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.engramstudio.domain.model.DashboardStats;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;
import com.speed.engramstudio.infrastructure.engram.dto.StatsDto;

import java.util.concurrent.CompletableFuture;

public class StatsApi {

    private final EngramHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public StatsApi(EngramHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<DashboardStats> getStats() {
        return httpClient.get("/stats")
            .thenApply(response -> {
                try {
                    StatsDto dto = objectMapper.readValue(response, StatsDto.class);
                    return new DashboardStats(
                        dto.totalObservations(),
                        dto.totalSessions(),
                        dto.totalPrompts(),
                        dto.projects() != null ? dto.projects() : java.util.List.of()
                    );
                } catch (Exception e) {
                    return DashboardStats.empty();
                }
            })
            .exceptionally(throwable -> DashboardStats.empty());
    }
}