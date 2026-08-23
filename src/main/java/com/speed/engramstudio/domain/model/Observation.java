package com.speed.engramstudio.domain.model;

import java.time.LocalDateTime;

public record Observation(
    long id,
    String syncId,
    String sessionId,
    ObservationType type,
    String title,
    String content,
    String project,
    String scope,
    String topicKey,
    LocalDateTime createdAt
) {
    public static Observation empty() {
        return new Observation(0, null, null, ObservationType.UNKNOWN, "", "", "", "", null, LocalDateTime.MIN);
    }
}
