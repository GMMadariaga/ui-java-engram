package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DiagnosticsReportDto(
    @JsonProperty("status") String status,
    @JsonProperty("project") String project,
    @JsonProperty("summary") DiagnosticsSummaryDto summary,
    @JsonProperty("checks") List<CheckResultDto> checks
) {}
