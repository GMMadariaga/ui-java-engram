package com.speed.engramstudio.infrastructure.engram.mapper;

import com.speed.engramstudio.domain.model.CheckResult;
import com.speed.engramstudio.domain.model.DiagnosticsReport;
import com.speed.engramstudio.domain.model.DiagnosticsSummary;
import com.speed.engramstudio.infrastructure.engram.dto.CheckResultDto;
import com.speed.engramstudio.infrastructure.engram.dto.DiagnosticsReportDto;
import com.speed.engramstudio.infrastructure.engram.dto.DiagnosticsSummaryDto;

import java.util.ArrayList;
import java.util.List;

public class DiagnosticsMapper {

    public static DiagnosticsReport toDomain(DiagnosticsReportDto dto) {
        if (dto == null) return DiagnosticsReport.empty();
        return new DiagnosticsReport(
            dto.status(),
            dto.project(),
            toDomain(dto.summary()),
            toChecks(dto.checks())
        );
    }

    public static DiagnosticsSummary toDomain(DiagnosticsSummaryDto dto) {
        if (dto == null) return DiagnosticsSummary.empty();
        return new DiagnosticsSummary(
            dto.total(),
            dto.ok(),
            dto.warnings(),
            dto.blocked(),
            dto.errors()
        );
    }

    public static CheckResult toDomain(CheckResultDto dto) {
        if (dto == null) return CheckResult.empty();
        return new CheckResult(
            dto.checkId(),
            dto.result(),
            dto.severity(),
            dto.reasonCode(),
            dto.message(),
            dto.why(),
            dto.evidence() != null ? dto.evidence() : java.util.Map.of(),
            dto.safeNextStep(),
            dto.requiresConfirmation()
        );
    }

    private static List<CheckResult> toChecks(List<CheckResultDto> dtos) {
        if (dtos == null) return List.of();
        List<CheckResult> result = new ArrayList<>(dtos.size());
        for (CheckResultDto dto : dtos) {
            result.add(toDomain(dto));
        }
        return result;
    }
}
