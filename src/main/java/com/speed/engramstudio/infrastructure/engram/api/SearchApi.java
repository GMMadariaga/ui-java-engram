package com.speed.engramstudio.infrastructure.engram.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;
import com.speed.engramstudio.infrastructure.engram.dto.ObservationDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchApi {

    private final EngramHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public SearchApi(EngramHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<ObservationDto>> search(String query) {
        return httpClient.get("/observations?q=" + encodeQuery(query))
            .thenApply(response -> {
                try {
                    return objectMapper.readValue(response, new TypeReference<List<ObservationDto>>() {});
                } catch (Exception e) {
                    return List.<ObservationDto>of();
                }
            })
            .exceptionally(throwable -> List.of());
    }

    private String encodeQuery(String query) {
        if (query == null || query.isBlank()) return "";
        return query.replace(" ", "%20").replace("&", "%26");
    }
}
