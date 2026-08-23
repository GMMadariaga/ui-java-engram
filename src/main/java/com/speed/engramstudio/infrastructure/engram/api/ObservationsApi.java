package com.speed.engramstudio.infrastructure.engram.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;
import com.speed.engramstudio.infrastructure.engram.dto.ObservationDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ObservationsApi {

    private final EngramHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ObservationsApi(EngramHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<ObservationDto>> getAll() {
        return httpClient.get("/observations")
            .thenApply(response -> {
                try {
                    return objectMapper.readValue(response, new TypeReference<List<ObservationDto>>() {});
                } catch (Exception e) {
                    return List.<ObservationDto>of();
                }
            })
            .exceptionally(throwable -> List.of());
    }

    public CompletableFuture<List<ObservationDto>> getRecent() {
        return httpClient.get("/observations/recent")
            .thenApply(response -> {
                try {
                    return objectMapper.readValue(response, new TypeReference<List<ObservationDto>>() {});
                } catch (Exception e) {
                    return List.<ObservationDto>of();
                }
            })
            .exceptionally(throwable -> List.of());
    }

    public CompletableFuture<ObservationDto> getById(long id) {
        return httpClient.get("/observations/" + id)
            .thenApply(response -> {
                try {
                    return objectMapper.readValue(response, ObservationDto.class);
                } catch (Exception e) {
                    return null;
                }
            })
            .exceptionally(throwable -> null);
    }
}