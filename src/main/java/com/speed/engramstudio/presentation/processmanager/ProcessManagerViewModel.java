package com.speed.engramstudio.presentation.processmanager;

import com.speed.engramstudio.application.processmanager.DetectEngram;
import com.speed.engramstudio.application.processmanager.RestartEngram;
import com.speed.engramstudio.application.processmanager.StartEngram;
import com.speed.engramstudio.application.processmanager.StopEngram;
import com.speed.engramstudio.infrastructure.process.EngramProcessManager;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ProcessManagerViewModel {

    private final DetectEngram detectEngram;
    private final StartEngram startEngram;
    private final StopEngram stopEngram;
    private final RestartEngram restartEngram;

    private final StringProperty processInfo = new SimpleStringProperty("Unknown");
    private final StringProperty processStatus = new SimpleStringProperty("Detecting...");
    private final StringProperty actionResult = new SimpleStringProperty("");
    private volatile boolean isRunning = false;

    public ProcessManagerViewModel(DetectEngram detectEngram, StartEngram startEngram,
                                    StopEngram stopEngram, RestartEngram restartEngram) {
        this.detectEngram = detectEngram;
        this.startEngram = startEngram;
        this.stopEngram = stopEngram;
        this.restartEngram = restartEngram;
    }

    public void detect() {
        processStatus.set("Detecting Engram...");
        detectEngram.execute()
            .thenAccept(status -> Platform.runLater(() -> {
                isRunning = status.running();
                processStatus.set(status.running() ? "RUNNING" : "STOPPED");
                processInfo.set(status.info());
                actionResult.set("");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    processStatus.set("ERROR");
                    processInfo.set(throwable.getMessage());
                });
                return null;
            });
    }

    public void start() {
        actionResult.set("Starting Engram...");
        startEngram.execute()
            .thenAccept(result -> Platform.runLater(() -> {
                actionResult.set(result.message());
                if (result.success()) {
                    isRunning = true;
                    processStatus.set("RUNNING");
                }
                detect();
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> actionResult.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public void stop() {
        actionResult.set("Stopping Engram...");
        stopEngram.execute()
            .thenAccept(result -> Platform.runLater(() -> {
                actionResult.set(result.message());
                if (result.success()) {
                    isRunning = false;
                    processStatus.set("STOPPED");
                }
                detect();
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> actionResult.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public void restart() {
        actionResult.set("Restarting Engram...");
        restartEngram.execute()
            .thenAccept(result -> Platform.runLater(() -> {
                actionResult.set(result.message());
                detect();
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> actionResult.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public StringProperty processInfoProperty() { return processInfo; }
    public StringProperty processStatusProperty() { return processStatus; }
    public StringProperty actionResultProperty() { return actionResult; }
    public boolean isRunning() { return isRunning; }
}
