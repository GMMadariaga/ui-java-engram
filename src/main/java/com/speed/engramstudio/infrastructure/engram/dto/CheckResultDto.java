package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CheckResultDto(
    @JsonProperty("check_id") String checkId,
    @JsonProperty("result") String result,
    @JsonProperty("severity") String severity,
    @JsonProperty("reason_code") String reasonCode,
    @JsonProperty("message") String message,
    @JsonProperty("why") String why,
    @JsonProperty("evidence") Map<String, Object> evidence,
    @JsonProperty("safe_next_step") String safeNextStep,
    @JsonProperty("requires_confirmation") boolean requiresConfirmation
) {}
