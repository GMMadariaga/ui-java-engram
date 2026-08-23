package com.speed.engramstudio.infrastructure.engram.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDetailDto;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SessionsApi {

    private final EngramHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SessionsApi(EngramHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<SessionDto>> getRecent() {
        return httpClient.get("/sessions/recent?limit=1000")
            .thenApply(response -> {
                try {
                    return objectMapper.readValue(response, new TypeReference<List<SessionDto>>() {});
                } catch (Exception e) {
                    return List.<SessionDto>of();
                }
            })
            .exceptionally(throwable -> List.of());
    }

    public CompletableFuture<SessionDetailDto> getById(String id) {
        return httpClient.get("/sessions/" + id)
            .thenApply(response -> {
                try {
                    return objectMapper.readValue(response, SessionDetailDto.class);
                } catch (Exception e) {
                    return null;
                }
            })
            .exceptionally(throwable -> null);
    }
}
