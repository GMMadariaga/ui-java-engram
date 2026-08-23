package com.speed.engramstudio.infrastructure.engram.mapper;

import com.speed.engramstudio.domain.model.Session;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDetailDto;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class SessionMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Session toDomain(SessionDto dto) {
        if (dto == null) return Session.empty();
        return new Session(
            dto.id(),
            dto.project(),
            "",
            parseDate(dto.startedAt()),
            dto.observationCount()
        );
    }

    public static Session toDomainDetail(SessionDetailDto dto) {
        if (dto == null) return Session.empty();
        return new Session(
            dto.id(),
            dto.project(),
            dto.directory(),
            parseDate(dto.startedAt()),
            0
        );
    }

    public static List<Session> toDomain(List<SessionDto> dtos) {
        if (dtos == null) return List.of();
        List<Session> result = new ArrayList<>(dtos.size());
        for (SessionDto dto : dtos) {
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
