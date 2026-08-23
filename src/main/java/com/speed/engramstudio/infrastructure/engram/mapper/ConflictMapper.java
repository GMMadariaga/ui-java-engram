package com.speed.engramstudio.infrastructure.engram.mapper;

import com.speed.engramstudio.domain.model.Conflict;
import com.speed.engramstudio.domain.model.JudgmentStatus;
import com.speed.engramstudio.domain.model.RelationType;
import com.speed.engramstudio.infrastructure.engram.dto.ConflictDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ConflictMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Conflict toDomain(ConflictDto dto) {
        if (dto == null) return Conflict.empty();
        return new Conflict(
            dto.id(),
            dto.syncId(),
            RelationType.fromString(dto.relation()),
            JudgmentStatus.fromString(dto.judgmentStatus()),
            dto.sourceId(),
            dto.sourceTitle(),
            dto.targetId(),
            dto.targetTitle(),
            parseDate(dto.createdAt()),
            parseDate(dto.updatedAt())
        );
    }

    public static List<Conflict> toDomain(List<ConflictDto> dtos) {
        if (dtos == null) return List.of();
        List<Conflict> result = new ArrayList<>(dtos.size());
        for (ConflictDto dto : dtos) {
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
