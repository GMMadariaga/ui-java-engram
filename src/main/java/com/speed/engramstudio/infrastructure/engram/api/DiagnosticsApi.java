package com.speed.engramstudio.infrastructure.engram.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;
import com.speed.engramstudio.infrastructure.engram.dto.DiagnosticsReportDto;

import java.util.concurrent.CompletableFuture;

public class DiagnosticsApi {

    private final EngramHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public DiagnosticsApi(EngramHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<DiagnosticsReportDto> runDoctor() {
        return httpClient.get("/doctor")
            .thenApply(response -> {
                try {
                    return objectMapper.readValue(response, DiagnosticsReportDto.class);
                } catch (Exception e) {
                    return null;
                }
            })
            .exceptionally(throwable -> null);
    }
}
