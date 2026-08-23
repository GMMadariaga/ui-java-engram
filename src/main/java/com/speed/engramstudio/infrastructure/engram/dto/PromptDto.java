package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PromptDto(
    @JsonProperty("id") long id,
    @JsonProperty("sync_id") String syncId,
    @JsonProperty("session_id") String sessionId,
    @JsonProperty("content") String content,
    @JsonProperty("project") String project,
    @JsonProperty("created_at") String createdAt
) {}
