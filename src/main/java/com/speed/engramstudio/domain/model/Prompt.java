package com.speed.engramstudio.domain.model;

import java.time.LocalDateTime;

public record Prompt(
    long id,
    String syncId,
    String sessionId,
    String content,
    String project,
    LocalDateTime createdAt
) {
    public static Prompt empty() {
        return new Prompt(0, null, null, "", "", LocalDateTime.MIN);
    }
}
