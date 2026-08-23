package com.speed.engramstudio.infrastructure.engram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ConflictDto(
    @JsonProperty("id") long id,
    @JsonProperty("sync_id") String syncId,
    @JsonProperty("relation") String relation,
    @JsonProperty("judgment_status") String judgmentStatus,
    @JsonProperty("source_id") String sourceId,
    @JsonProperty("source_title") String sourceTitle,
    @JsonProperty("target_id") String targetId,
    @JsonProperty("target_title") String targetTitle,
    @JsonProperty("created_at") String createdAt,
    @JsonProperty("updated_at") String updatedAt
) {}
