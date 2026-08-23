package com.speed.engramstudio.infrastructure.engram;

import com.speed.engramstudio.domain.model.Conflict;
import com.speed.engramstudio.domain.repository.ConflictRepository;
import com.speed.engramstudio.infrastructure.engram.api.ConflictsApi;
import com.speed.engramstudio.infrastructure.engram.mapper.ConflictMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ConflictRepositoryImpl implements ConflictRepository {

    private final ConflictsApi conflictsApi;

    public ConflictRepositoryImpl(ConflictsApi conflictsApi) {
        this.conflictsApi = conflictsApi;
    }

    @Override
    public CompletableFuture<List<Conflict>> getAll() {
        return conflictsApi.getAll()
            .thenApply(ConflictMapper::toDomain);
    }
}
