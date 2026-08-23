package com.speed.engramstudio.infrastructure.engram;

import com.speed.engramstudio.domain.model.DiagnosticsReport;
import com.speed.engramstudio.domain.repository.DiagnosticsRepository;
import com.speed.engramstudio.infrastructure.engram.api.DiagnosticsApi;
import com.speed.engramstudio.infrastructure.engram.mapper.DiagnosticsMapper;

import java.util.concurrent.CompletableFuture;

public class DiagnosticsRepositoryImpl implements DiagnosticsRepository {

    private final DiagnosticsApi diagnosticsApi;

    public DiagnosticsRepositoryImpl(DiagnosticsApi diagnosticsApi) {
        this.diagnosticsApi = diagnosticsApi;
    }

    @Override
    public CompletableFuture<DiagnosticsReport> runDoctor() {
        return diagnosticsApi.runDoctor()
            .thenApply(DiagnosticsMapper::toDomain);
    }
}
