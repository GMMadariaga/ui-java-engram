package com.speed.engramstudio.domain.model;

public enum JudgmentStatus {
    PENDING,
    JUDGED,
    UNKNOWN;

    public static JudgmentStatus fromString(String value) {
        if (value == null) return UNKNOWN;
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
