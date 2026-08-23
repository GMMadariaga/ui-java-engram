package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionDto(
    @JsonProperty("id") String id,
    @JsonProperty("project") String project,
    @JsonProperty("started_at") String startedAt,
    @JsonProperty("observation_count") int observationCount
) {}