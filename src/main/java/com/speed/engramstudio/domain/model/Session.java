package com.speed.engramstudio.domain.model;

import java.time.LocalDateTime;

public record Session(
    String id,
    String project,
    String directory,
    LocalDateTime startedAt,
    int observationCount
) {
    public static Session empty() {
        return new Session("", "", "", LocalDateTime.MIN, 0);
    }
}
