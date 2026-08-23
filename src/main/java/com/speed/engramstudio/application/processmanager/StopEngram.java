package com.speed.engramstudio.application.processmanager;

import com.speed.engramstudio.infrastructure.process.EngramProcessManager;

import java.util.concurrent.CompletableFuture;

public class StopEngram {

    private final EngramProcessManager processManager;

    public StopEngram(EngramProcessManager processManager) {
        this.processManager = processManager;
    }

    public CompletableFuture<EngramProcessManager.ProcessResult> execute() {
        return processManager.stop();
    }
}
