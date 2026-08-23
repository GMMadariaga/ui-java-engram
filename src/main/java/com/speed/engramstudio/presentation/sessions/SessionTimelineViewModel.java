package com.speed.engramstudio.presentation.sessions;

import com.speed.engramstudio.application.memory.GetRecentMemories;
import com.speed.engramstudio.application.sessions.GetRecentSessions;
import com.speed.engramstudio.application.sessions.GetSessionDetail;
import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.model.Session;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.stream.Collectors;

public class SessionTimelineViewModel {

    private final GetRecentSessions getRecentSessions;
    private final GetSessionDetail getSessionDetail;
    private final GetRecentMemories getRecentMemories;

    private final ObservableList<Session> sessions = FXCollections.observableArrayList();
    private final ObservableList<Observation> sessionObservations = FXCollections.observableArrayList();
    private final ObjectProperty<Session> selectedSession = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    public SessionTimelineViewModel(GetRecentSessions getRecentSessions,
                                     GetSessionDetail getSessionDetail,
                                     GetRecentMemories getRecentMemories) {
        this.getRecentSessions = getRecentSessions;
        this.getSessionDetail = getSessionDetail;
        this.getRecentMemories = getRecentMemories;
    }

    public void load() {
        statusMessage.set("Loading sessions...");
        getRecentSessions.execute()
            .thenAccept(list -> Platform.runLater(() -> {
                sessions.clear();
                sessions.addAll(list);
                statusMessage.set("Loaded " + list.size() + " sessions");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public void selectSession(Session session) {
        if (session == null) {
            sessionObservations.clear();
            return;
        }
        selectedSession.set(session);
        statusMessage.set("Loading session detail: " + session.id() + "...");

        // Load all observations and filter by session_id
        getRecentMemories.execute()
            .thenAccept(list -> Platform.runLater(() -> {
                sessionObservations.clear();
                List<Observation> filtered = list.stream()
                    .filter(o -> session.id().equals(o.sessionId()))
                    .collect(Collectors.toList());
                sessionObservations.addAll(filtered);
                statusMessage.set("Session " + session.id() + ": " + filtered.size() + " observations");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public ObservableList<Session> sessions() { return sessions; }
    public ObservableList<Observation> sessionObservations() { return sessionObservations; }
    public ObjectProperty<Session> selectedSessionProperty() { return selectedSession; }
    public StringProperty statusMessageProperty() { return statusMessage; }
}
