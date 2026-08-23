package com.speed.engramstudio.domain.model;

import java.time.LocalDateTime;

public record Conflict(
    long id,
    String syncId,
    RelationType relation,
    JudgmentStatus judgmentStatus,
    String sourceId,
    String sourceTitle,
    String targetId,
    String targetTitle,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static Conflict empty() {
        return new Conflict(0, null, RelationType.UNKNOWN, JudgmentStatus.UNKNOWN, "", "", "", "", LocalDateTime.MIN, LocalDateTime.MIN);
    }
}
