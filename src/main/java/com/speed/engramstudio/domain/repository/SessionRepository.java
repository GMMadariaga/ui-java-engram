package com.speed.engramstudio.domain.repository;

import com.speed.engramstudio.domain.model.Session;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SessionRepository {
    CompletableFuture<List<Session>> getRecent();
    CompletableFuture<Session> getById(String id);
}
