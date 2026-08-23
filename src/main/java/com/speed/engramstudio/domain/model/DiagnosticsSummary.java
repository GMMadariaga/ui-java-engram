package com.speed.engramstudio.domain.model;

public record DiagnosticsSummary(
    int total,
    int ok,
    int warnings,
    int blocked,
    int errors
) {
    public static DiagnosticsSummary empty() {
        return new DiagnosticsSummary(0, 0, 0, 0, 0);
    }
}
