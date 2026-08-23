package com.speed.engramstudio.domain.repository;

import com.speed.engramstudio.domain.model.ConnectionStatus;
import com.speed.engramstudio.domain.model.EngramVersion;
import java.util.concurrent.CompletableFuture;

public interface ConnectionRepository {
    
    CompletableFuture<ConnectionStatus> checkHealth();
    
    CompletableFuture<EngramVersion> getVersion();
    
    CompletableFuture<Boolean> ping();
}