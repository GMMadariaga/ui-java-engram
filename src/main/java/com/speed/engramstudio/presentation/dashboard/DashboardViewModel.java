package com.speed.engramstudio.presentation.dashboard;

import com.speed.engramstudio.application.dashboard.GetDashboardStats;
import com.speed.engramstudio.domain.model.DashboardStats;
import com.speed.engramstudio.infrastructure.engram.dto.ObservationDto;
import com.speed.engramstudio.infrastructure.engram.dto.SessionDto;
import javafx.application.Platform;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DashboardViewModel {

    private static final Logger logger = LoggerFactory.getLogger(DashboardViewModel.class);

    private final GetDashboardStats getDashboardStats;

    private final LongProperty observations = new SimpleLongProperty(0);
    private final LongProperty sessions = new SimpleLongProperty(0);
    private final LongProperty prompts = new SimpleLongProperty(0);
    private final StringProperty projectsText = new SimpleStringProperty("");
    private final ObservableList<ObservationDto> recentObservations = FXCollections.observableArrayList();
    private final ObservableList<SessionDto> recentSessions = FXCollections.observableArrayList();
    private final StringProperty statusMessage = new SimpleStringProperty("Loading...");

    public DashboardViewModel(GetDashboardStats getDashboardStats) {
        this.getDashboardStats = getDashboardStats;
    }

    public void load() {
        logger.info("Dashboard loading...");
        statusMessage.set("Querying Engram...");

        getDashboardStats.execute()
            .thenAccept(stats -> {
                logger.info("Stats received: obs={}, sess={}, prompts={}", stats.totalObservations(), stats.totalSessions(), stats.totalPrompts());
                Platform.runLater(() -> {
                    observations.set(stats.totalObservations());
                    sessions.set(stats.totalSessions());
                    prompts.set(stats.totalPrompts());
                    projectsText.set(String.join(", ", stats.projects()));
                    statusMessage.set("OK");
                });
            })
            .exceptionally(throwable -> {
                logger.error("Failed to load stats", throwable);
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });

        getDashboardStats.getRecentObservations()
            .thenAccept(list -> {
                logger.info("Observations received: {} items", list.size());
                Platform.runLater(() -> {
                    recentObservations.clear();
                    recentObservations.addAll(list);
                });
            })
            .exceptionally(throwable -> {
                logger.error("Failed to load observations", throwable);
                return null;
            });

        getDashboardStats.getRecentSessions()
            .thenAccept(list -> {
                logger.info("Sessions received: {} items", list.size());
                Platform.runLater(() -> {
                    recentSessions.clear();
                    recentSessions.addAll(list);
                });
            })
            .exceptionally(throwable -> {
                logger.error("Failed to load sessions", throwable);
                return null;
            });
    }

    public LongProperty observationsProperty() { return observations; }
    public LongProperty sessionsProperty() { return sessions; }
    public LongProperty promptsProperty() { return prompts; }
    public StringProperty projectsTextProperty() { return projectsText; }
    public ObservableList<ObservationDto> recentObservations() { return recentObservations; }
    public ObservableList<SessionDto> recentSessions() { return recentSessions; }
    public StringProperty statusMessageProperty() { return statusMessage; }
}
