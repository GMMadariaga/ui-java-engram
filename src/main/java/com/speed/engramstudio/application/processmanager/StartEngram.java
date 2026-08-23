package com.speed.engramstudio.application.processmanager;

import com.speed.engramstudio.infrastructure.process.EngramProcessManager;

import java.util.concurrent.CompletableFuture;

public class StartEngram {

    private final EngramProcessManager processManager;

    public StartEngram(EngramProcessManager processManager) {
        this.processManager = processManager;
    }

    public CompletableFuture<EngramProcessManager.ProcessResult> execute() {
        return processManager.start();
    }
}
