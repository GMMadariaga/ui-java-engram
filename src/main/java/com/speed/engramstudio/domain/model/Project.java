package com.speed.engramstudio.domain.model;

public record Project(
    String name,
    int observationCount,
    int sessionCount
) {
    public static Project empty() {
        return new Project("", 0, 0);
    }
}
