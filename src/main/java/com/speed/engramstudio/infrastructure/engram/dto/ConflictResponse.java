package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConflictResponse(
    @JsonProperty("relations") List<ConflictDto> relations,
    @JsonProperty("total") int total,
    @JsonProperty("limit") int limit,
    @JsonProperty("offset") int offset
) {}
