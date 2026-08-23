package com.speed.engramstudio.infrastructure.engram;

import com.speed.engramstudio.domain.model.Session;
import com.speed.engramstudio.domain.repository.SessionRepository;
import com.speed.engramstudio.infrastructure.engram.api.SessionsApi;
import com.speed.engramstudio.infrastructure.engram.mapper.SessionMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SessionRepositoryImpl implements SessionRepository {

    private final SessionsApi sessionsApi;

    public SessionRepositoryImpl(SessionsApi sessionsApi) {
        this.sessionsApi = sessionsApi;
    }

    @Override
    public CompletableFuture<List<Session>> getRecent() {
        return sessionsApi.getRecent()
            .thenApply(SessionMapper::toDomain);
    }

    @Override
    public CompletableFuture<Session> getById(String id) {
        return sessionsApi.getById(id)
            .thenApply(SessionMapper::toDomainDetail);
    }
}
