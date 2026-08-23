package com.speed.engramstudio.presentation.prompts;

import com.speed.engramstudio.application.prompts.GetRecentPrompts;
import com.speed.engramstudio.domain.model.Prompt;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PromptsViewModel {

    private final GetRecentPrompts getRecentPrompts;

    private final ObservableList<Prompt> prompts = FXCollections.observableArrayList();
    private final ObjectProperty<Prompt> selectedPrompt = new SimpleObjectProperty<>();
    private final StringProperty statusMessage = new SimpleStringProperty("Ready");

    public PromptsViewModel(GetRecentPrompts getRecentPrompts) {
        this.getRecentPrompts = getRecentPrompts;
    }

    public void load() {
        statusMessage.set("Loading prompts...");
        getRecentPrompts.execute()
            .thenAccept(list -> Platform.runLater(() -> {
                prompts.clear();
                prompts.addAll(list);
                statusMessage.set("Loaded " + list.size() + " prompts");
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> statusMessage.set("Error: " + throwable.getMessage()));
                return null;
            });
    }

    public ObservableList<Prompt> prompts() { return prompts; }
    public ObjectProperty<Prompt> selectedPromptProperty() { return selectedPrompt; }
    public StringProperty statusMessageProperty() { return statusMessage; }
}
