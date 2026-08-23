package com.speed.engramstudio.domain.model;

public enum RelationType {
    RELATED,
    COMPATIBLE,
    SCOPED,
    CONFLICTS_WITH,
    SUPERSEDES,
    NOT_CONFLICT,
    UNKNOWN;

    public static RelationType fromString(String value) {
        if (value == null) return UNKNOWN;
        try {
            return valueOf(value.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
