package com.speed.engramstudio.application.conflicts;

import com.speed.engramstudio.domain.model.Conflict;
import com.speed.engramstudio.domain.repository.ConflictRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetConflicts {

    private final ConflictRepository repository;

    public GetConflicts(ConflictRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<List<Conflict>> execute() {
        return repository.getAll();
    }
}
