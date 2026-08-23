package com.speed.engramstudio.infrastructure.engram.mapper;

import com.speed.engramstudio.domain.model.Prompt;
import com.speed.engramstudio.infrastructure.engram.dto.PromptDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PromptMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Prompt toDomain(PromptDto dto) {
        if (dto == null) return Prompt.empty();
        return new Prompt(
            dto.id(),
            dto.syncId(),
            dto.sessionId(),
            dto.content(),
            dto.project(),
            parseDate(dto.createdAt())
        );
    }

    public static List<Prompt> toDomain(List<PromptDto> dtos) {
        if (dtos == null) return List.of();
        List<Prompt> result = new ArrayList<>(dtos.size());
        for (PromptDto dto : dtos) {
            result.add(toDomain(dto));
        }
        return result;
    }

    private static LocalDateTime parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return LocalDateTime.MIN;
        try {
            return LocalDateTime.parse(dateStr, FORMATTER);
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(dateStr.substring(0, 19), FORMATTER);
            } catch (Exception ex) {
                return LocalDateTime.MIN;
            }
        }
    }
}
