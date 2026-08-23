package com.speed.engramstudio.presentation.diagnostics;

import com.speed.engramstudio.application.diagnostics.RunDiagnostics;
import com.speed.engramstudio.domain.model.CheckResult;
import com.speed.engramstudio.domain.model.DiagnosticsReport;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DiagnosticsViewModel {

    private final RunDiagnostics runDiagnostics;

    private final ObservableList<CheckResult> checks = FXCollections.observableArrayList();
    private final ObjectProperty<DiagnosticsReport> report = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    public DiagnosticsViewModel(RunDiagnostics runDiagnostics) {
        this.runDiagnostics = runDiagnostics;
    }

    public void load() {
        statusMessage.set("Running diagnostics...");
        runDiagnostics.execute()
            .thenAccept(r -> Platform.runLater(() -> {
                report.set(r);
                checks.clear();
                checks.addAll(r.checks());
                statusMessage.set("Doctor: " + r.status().toUpperCase() +
                    " | " + r.summary().ok() + " ok, " +
                    r.summary().warnings() + " warnings, " +
                    r.summary().errors() + " errors");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public ObservableList<CheckResult> checks() { return checks; }
    public ObjectProperty<DiagnosticsReport> reportProperty() { return report; }
    public StringProperty statusMessageProperty() { return statusMessage; }
}
