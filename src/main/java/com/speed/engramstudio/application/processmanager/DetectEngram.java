package com.speed.engramstudio.application.processmanager;

import com.speed.engramstudio.infrastructure.process.EngramProcessManager;

import java.util.concurrent.CompletableFuture;

public class DetectEngram {

    private final EngramProcessManager processManager;

    public DetectEngram(EngramProcessManager processManager) {
        this.processManager = processManager;
    }

    public CompletableFuture<EngramProcessManager.ProcessStatus> execute() {
        return processManager.detect();
    }
}
