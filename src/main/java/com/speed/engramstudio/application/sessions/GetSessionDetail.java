package com.speed.engramstudio.application.sessions;

import com.speed.engramstudio.domain.model.Session;
import com.speed.engramstudio.domain.repository.SessionRepository;

import java.util.concurrent.CompletableFuture;

public class GetSessionDetail {

    private final SessionRepository repository;

    public GetSessionDetail(SessionRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<Session> execute(String sessionId) {
        return repository.getById(sessionId);
    }
}
