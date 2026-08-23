package com.speed.engramstudio.domain.model;

import java.util.List;

public record DashboardStats(
    long totalObservations,
    long totalSessions,
    long totalPrompts,
    List<String> projects
) {
    public static DashboardStats empty() {
        return new DashboardStats(0, 0, 0, List.of());
    }
}