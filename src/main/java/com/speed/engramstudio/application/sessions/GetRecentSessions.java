package com.speed.engramstudio.application.sessions;

import com.speed.engramstudio.domain.model.Session;
import com.speed.engramstudio.domain.repository.SessionRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetRecentSessions {

    private final SessionRepository repository;

    public GetRecentSessions(SessionRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<List<Session>> execute() {
        return repository.getRecent();
    }
}
