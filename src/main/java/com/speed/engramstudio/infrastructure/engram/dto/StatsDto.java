package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StatsDto(
    @JsonProperty("total_observations") long totalObservations,
    @JsonProperty("total_sessions") long totalSessions,
    @JsonProperty("total_prompts") long totalPrompts,
    @JsonProperty("projects") List<String> projects
) {}