package com.speed.engramstudio.presentation.components;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Formats API timestamps without assuming LocalDateTime.toString() has fixed length. */
public final class DateTimeDisplay {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private DateTimeDisplay() {
    }

    public static String format(LocalDateTime value) {
        if (value == null || LocalDateTime.MIN.equals(value)) {
            return "";
        }
        return FORMATTER.format(value);
    }

    public static String formatOr(LocalDateTime value, String fallback) {
        String formatted = format(value);
        return formatted.isEmpty() ? fallback : formatted;
    }
}
