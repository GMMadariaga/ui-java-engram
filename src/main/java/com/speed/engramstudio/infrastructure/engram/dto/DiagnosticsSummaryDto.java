package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiagnosticsSummaryDto(
    @JsonProperty("total") int total,
    @JsonProperty("ok") int ok,
    @JsonProperty("warnings") int warnings,
    @JsonProperty("blocked") int blocked,
    @JsonProperty("errors") int errors
) {}
