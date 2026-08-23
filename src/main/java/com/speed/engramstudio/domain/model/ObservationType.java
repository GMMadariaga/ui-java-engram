package com.speed.engramstudio.domain.model;

public enum ObservationType {
    ARCHITECTURE,
    BUGFIX,
    DECISION,
    DISCOVERY,
    LEARNING,
    PATTERN,
    PREFERENCE,
    SESSION_SUMMARY,
    UNKNOWN;

    public static ObservationType fromString(String value) {
        if (value == null) return UNKNOWN;
        try {
            return valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
