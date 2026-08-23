package com.speed.engramstudio.infrastructure.engram.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;
import com.speed.engramstudio.infrastructure.engram.dto.ConflictDto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class ConflictsApi {

    private static final Logger logger = Logger.getLogger(ConflictsApi.class.getName());
    private static final List<ConflictDto> EMPTY = Collections.emptyList();
    private final EngramHttpClient httpClient;
    private final ObjectMapper objectMapper;

    public ConflictsApi(EngramHttpClient httpClient) {
        this.httpClient = httpClient;
        this.objectMapper = new ObjectMapper();
    }

    public CompletableFuture<List<ConflictDto>> getAll() {
        return httpClient.get("/conflicts")
            .thenApply(response -> {
                try {
                    JsonNode root = objectMapper.readTree(response);
                    JsonNode relations = root.get("relations");
                    if (relations != null && relations.isArray()) {
                        List<ConflictDto> result = new ArrayList<>();
                        for (JsonNode node : relations) {
                            result.add(objectMapper.treeToValue(node, ConflictDto.class));
                        }
                        logger.info("Parsed " + result.size() + " relations from API");
                        return result;
                    }
                    logger.warning("No 'relations' array in response");
                    return EMPTY;
                } catch (Exception e) {
                    logger.severe("Failed to parse conflicts: " + e.getMessage());
                    return EMPTY;
                }
            })
            .exceptionally(throwable -> {
                logger.severe("Failed to fetch conflicts: " + throwable.getMessage());
                return EMPTY;
            });
    }
}
