package com.speed.engramstudio.application.diagnostics;

import com.speed.engramstudio.domain.model.DiagnosticsReport;
import com.speed.engramstudio.domain.repository.DiagnosticsRepository;

import java.util.concurrent.CompletableFuture;

public class RunDiagnostics {

    private final DiagnosticsRepository repository;

    public RunDiagnostics(DiagnosticsRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<DiagnosticsReport> execute() {
        return repository.runDoctor();
    }
}
