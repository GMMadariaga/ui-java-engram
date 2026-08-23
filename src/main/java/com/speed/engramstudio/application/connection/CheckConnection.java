package com.speed.engramstudio.application.connection;

import com.speed.engramstudio.domain.model.ConnectionStatus;
import com.speed.engramstudio.domain.repository.ConnectionRepository;

import java.util.concurrent.CompletableFuture;

public class CheckConnection {
    
    private final ConnectionRepository connectionRepository;
    
    public CheckConnection(ConnectionRepository connectionRepository) {
        this.connectionRepository = connectionRepository;
    }
    
    public CompletableFuture<ConnectionStatus> execute() {
        return connectionRepository.checkHealth();
    }
}