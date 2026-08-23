package com.speed.engramstudio.infrastructure.engram.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;
import com.speed.engramstudio.infrastructure.engram.dto.PromptDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PromptsApi {

    private final EngramHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public PromptsApi(EngramHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<PromptDto>> getRecent() {
        return httpClient.get("/prompts/recent?limit=1000")
            .thenApply(response -> {
                try {
                    return objectMapper.readValue(response, new TypeReference<List<PromptDto>>() {});
                } catch (Exception e) {
                    return List.<PromptDto>of();
                }
            })
            .exceptionally(throwable -> List.of());
    }
}
