package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SessionDetailDto(
    @JsonProperty("id") String id,
    @JsonProperty("project") String project,
    @JsonProperty("directory") String directory,
    @JsonProperty("started_at") String startedAt
) {}
