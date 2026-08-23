package com.speed.engramstudio.presentation.memory;

import com.speed.engramstudio.application.memory.GetRecentMemories;
import com.speed.engramstudio.application.memory.SearchMemories;
import com.speed.engramstudio.domain.model.Observation;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class MemoryExplorerViewModel {

    private final GetRecentMemories getRecentMemories;
    private final SearchMemories searchMemories;

    private final ObservableList<Observation> observations = FXCollections.observableArrayList();
    private final ObjectProperty<Observation> selectedObservation = new SimpleObjectProperty<>();
    private final StringProperty searchQuery = new SimpleStringProperty("");
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    public MemoryExplorerViewModel(GetRecentMemories getRecentMemories, SearchMemories searchMemories) {
        this.getRecentMemories = getRecentMemories;
        this.searchMemories = searchMemories;
    }

    public void load() {
        loadRecent();
    }

    public void loadRecent() {
        statusMessage.set("Loading observations...");
        getRecentMemories.execute()
            .thenAccept(list -> Platform.runLater(() -> {
                observations.clear();
                observations.addAll(list);
                statusMessage.set("Loaded " + list.size() + " observations");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public void search() {
        String query = searchQuery.get();
        if (query == null || query.isBlank()) {
            loadRecent();
            return;
        }
        statusMessage.set("Searching: " + query + "...");
        searchMemories.execute(query)
            .thenAccept(list -> Platform.runLater(() -> {
                observations.clear();
                observations.addAll(list);
                statusMessage.set("Found " + list.size() + " results for \"" + query + "\"");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Search error: " + throwable.getMessage()));
                return null;
            });
    }

    public void clearSearch() {
        searchQuery.set("");
        loadRecent();
    }

    public ObservableList<Observation> observations() { return observations; }
    public ObjectProperty<Observation> selectedObservationProperty() { return selectedObservation; }
    public StringProperty searchQueryProperty() { return searchQuery; }
    public StringProperty statusMessageProperty() { return statusMessage; }
}
