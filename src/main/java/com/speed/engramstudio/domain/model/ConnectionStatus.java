package com.speed.engramstudio.domain.model;

import com.speed.engramstudio.domain.enums.ConnectionState;

public record ConnectionStatus(
    ConnectionState state,
    String url,
    EngramVersion version,
    String message
) {
    public static ConnectionStatus connected(String url, EngramVersion version) {
        return new ConnectionStatus(ConnectionState.CONNECTED, url, version, "Connected");
    }
    
    public static ConnectionStatus disconnected(String url) {
        return new ConnectionStatus(ConnectionState.DISCONNECTED, url, null, "Disconnected");
    }
    
    public static ConnectionStatus starting(String url) {
        return new ConnectionStatus(ConnectionState.STARTING, url, null, "Starting...");
    }
    
    public static ConnectionStatus error(String url, String message) {
        return new ConnectionStatus(ConnectionState.ERROR, url, null, message);
    }
}