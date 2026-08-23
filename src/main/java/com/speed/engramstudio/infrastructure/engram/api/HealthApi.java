package com.speed.engramstudio.infrastructure.engram.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;
import com.speed.engramstudio.domain.model.EngramVersion;

import java.util.concurrent.CompletableFuture;

public class HealthApi {
    
    private final EngramHttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public HealthApi(EngramHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }
    
    public CompletableFuture<Boolean> checkHealth() {
        return httpClient.get("/health")
            .thenApply(response -> {
                try {
                    JsonNode json = objectMapper.readTree(response);
                    return json.has("status") && 
                           json.get("status").asText().equals("ok");
                } catch (Exception e) {
                    return false;
                }
            })
            .exceptionally(throwable -> false);
    }
    
    public CompletableFuture<EngramVersion> getVersion() {
        return httpClient.get("/health")
            .thenApply(response -> {
                try {
                    JsonNode json = objectMapper.readTree(response);
                    if (json.has("version")) {
                        return EngramVersion.parse(json.get("version").asText());
                    }
                    return new EngramVersion(0, 0, 0);
                } catch (Exception e) {
                    return new EngramVersion(0, 0, 0);
                }
            })
            .exceptionally(throwable -> new EngramVersion(0, 0, 0));
    }
}