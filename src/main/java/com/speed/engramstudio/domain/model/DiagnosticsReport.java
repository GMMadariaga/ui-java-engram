package com.speed.engramstudio.domain.model;

import java.util.List;

public record DiagnosticsReport(
    String status,
    String project,
    DiagnosticsSummary summary,
    List<CheckResult> checks
) {
    public static DiagnosticsReport empty() {
        return new DiagnosticsReport("unknown", "", DiagnosticsSummary.empty(), List.of());
    }
}
