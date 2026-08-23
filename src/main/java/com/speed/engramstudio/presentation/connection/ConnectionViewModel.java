package com.speed.engramstudio.presentation.connection;

import com.speed.engramstudio.application.connection.CheckConnection;
import com.speed.engramstudio.domain.enums.ConnectionState;
import com.speed.engramstudio.domain.model.ConnectionStatus;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ConnectionViewModel {
    
    private final CheckConnection checkConnection;
    
    private final ObjectProperty<ConnectionState> state = new SimpleObjectProperty<>(ConnectionState.DISCONNECTED);
    private final StringProperty url = new SimpleStringProperty("");
    private final StringProperty version = new SimpleStringProperty("");
    private final StringProperty message = new SimpleStringProperty("");
    private final ObjectProperty<ConnectionStatus> status = new SimpleObjectProperty<>();
    
    public ConnectionViewModel(CheckConnection checkConnection) {
        this.checkConnection = checkConnection;
    }
    
    public void checkConnection() {
        state.set(ConnectionState.STARTING);
        message.set("Checking connection...");
        
        checkConnection.execute()
            .thenAccept(status -> Platform.runLater(() -> {
                this.status.set(status);
                this.state.set(status.state());
                this.url.set(status.url());
                this.version.set(status.version() != null ? status.version().toString() : "Unknown");
                this.message.set(status.message());
            }))
            .exceptionally(throwable -> {
                Platform.runLater(() -> {
                    state.set(ConnectionState.ERROR);
                    message.set("Error: " + throwable.getMessage());
                });
                return null;
            });
    }
    
    public ObjectProperty<ConnectionState> stateProperty() {
        return state;
    }
    
    public StringProperty urlProperty() {
        return url;
    }
    
    public StringProperty versionProperty() {
        return version;
    }
    
    public StringProperty messageProperty() {
        return message;
    }
    
    public ObjectProperty<ConnectionStatus> statusProperty() {
        return status;
    }
}