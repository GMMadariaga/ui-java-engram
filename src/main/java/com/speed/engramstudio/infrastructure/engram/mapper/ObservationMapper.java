package com.speed.engramstudio.infrastructure.engram.mapper;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.model.ObservationType;
import com.speed.engramstudio.infrastructure.engram.dto.ObservationDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class ObservationMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static Observation toDomain(ObservationDto dto) {
        if (dto == null) return Observation.empty();
        return new Observation(
            dto.id(),
            null,
            dto.sessionId(),
            ObservationType.fromString(dto.type()),
            dto.title(),
            dto.content(),
            dto.project(),
            dto.scope(),
            dto.topicKey(),
            parseDate(dto.createdAt())
        );
    }

    public static List<Observation> toDomain(List<ObservationDto> dtos) {
        if (dtos == null) return List.of();
        List<Observation> result = new ArrayList<>(dtos.size());
        for (ObservationDto dto : dtos) {
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
