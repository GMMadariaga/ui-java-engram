package com.speed.engramstudio.infrastructure.engram;

import com.speed.engramstudio.domain.model.ConnectionStatus;
import com.speed.engramstudio.domain.model.EngramVersion;
import com.speed.engramstudio.domain.repository.ConnectionRepository;
import com.speed.engramstudio.infrastructure.config.AppConfiguration;
import com.speed.engramstudio.infrastructure.engram.api.HealthApi;
import com.speed.engramstudio.infrastructure.engram.client.EngramHttpClient;

import java.util.concurrent.CompletableFuture;

public class EngramConnectionAdapter implements ConnectionRepository {
    
    private final AppConfiguration config;
    private final EngramHttpClient httpClient;
    private final HealthApi healthApi;
    
    public EngramConnectionAdapter(AppConfiguration config) {
        this.config = config;
        this.httpClient = new EngramHttpClient(
            config.getEngramUrl(),
            config.getTimeout()
        );
        this.healthApi = new HealthApi(httpClient);
    }
    
    @Override
    public CompletableFuture<ConnectionStatus> checkHealth() {
        return healthApi.checkHealth()
            .thenApply(isHealthy -> {
                if (isHealthy) {
                    EngramVersion version = healthApi.getVersion().join();
                    return ConnectionStatus.connected(config.getEngramUrl(), version);
                } else {
                    return ConnectionStatus.disconnected(config.getEngramUrl());
                }
            })
            .exceptionally(throwable -> 
                ConnectionStatus.error(config.getEngramUrl(), throwable.getMessage())
            );
    }
    
    @Override
    public CompletableFuture<EngramVersion> getVersion() {
        return healthApi.getVersion();
    }
    
    @Override
    public CompletableFuture<Boolean> ping() {
        return healthApi.checkHealth();
    }
    
    public EngramHttpClient getHttpClient() {
        return httpClient;
    }
    
    public HealthApi getHealthApi() {
        return healthApi;
    }
}