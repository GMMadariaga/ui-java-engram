package com.speed.engramstudio.application.memory;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.repository.ObservationRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetRecentMemories {

    private final ObservationRepository repository;

    public GetRecentMemories(ObservationRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<List<Observation>> execute() {
        return repository.getRecent();
    }
}
