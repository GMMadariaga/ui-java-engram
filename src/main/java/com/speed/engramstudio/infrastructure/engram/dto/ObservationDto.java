package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ObservationDto(
    @JsonProperty("id") long id,
    @JsonProperty("session_id") String sessionId,
    @JsonProperty("type") String type,
    @JsonProperty("title") String title,
    @JsonProperty("content") String content,
    @JsonProperty("project") String project,
    @JsonProperty("scope") String scope,
    @JsonProperty("topic_key") String topicKey,
    @JsonProperty("created_at") String createdAt
) {}