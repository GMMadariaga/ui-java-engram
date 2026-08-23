package com.speed.engramstudio.domain.model;

import java.util.Map;

public record CheckResult(
    String checkId,
    String result,
    String severity,
    String reasonCode,
    String message,
    String why,
    Map<String, Object> evidence,
    String safeNextStep,
    boolean requiresConfirmation
) {
    public static CheckResult empty() {
        return new CheckResult("", "", "", "", "", "", Map.of(), "", false);
    }

    public boolean isOk() {
        return "ok".equalsIgnoreCase(result);
    }

    public boolean isWarning() {
        return "warning".equalsIgnoreCase(result);
    }

    public boolean isError() {
        return "error".equalsIgnoreCase(result);
    }

    public boolean isBlocked() {
        return "blocked".equalsIgnoreCase(result);
    }
}
