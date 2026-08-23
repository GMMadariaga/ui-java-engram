package com.speed.engramstudio.domain.repository;

import com.speed.engramstudio.domain.model.DiagnosticsReport;

import java.util.concurrent.CompletableFuture;

public interface DiagnosticsRepository {
    CompletableFuture<DiagnosticsReport> runDoctor();
}
