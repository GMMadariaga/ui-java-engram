package com.speed.engramstudio.domain.repository;

import com.speed.engramstudio.domain.model.Conflict;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ConflictRepository {
    CompletableFuture<List<Conflict>> getAll();
}
