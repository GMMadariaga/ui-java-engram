package com.speed.engramstudio.presentation.conflicts;

import com.speed.engramstudio.application.conflicts.GetConflicts;
import com.speed.engramstudio.domain.model.Conflict;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConflictsViewModel {

    private final GetConflicts getConflicts;

    private final ObservableList<Conflict> conflicts = FXCollections.observableArrayList();
    private final ObjectProperty<Conflict> selectedConflict = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    public ConflictsViewModel(GetConflicts getConflicts) {
        this.getConflicts = getConflicts;
    }

    public void load() {
        statusMessage.set("Loading relations...");
        getConflicts.execute()
            .thenAccept(list -> Platform.runLater(() -> {
                conflicts.clear();
                conflicts.addAll(list);
                statusMessage.set("Loaded " + list.size() + " relations");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public ObservableList<Conflict> conflicts() { return conflicts; }
    public ObjectProperty<Conflict> selectedConflictProperty() { return selectedConflict; }
    public StringProperty statusMessageProperty() { return statusMessage; }
}
