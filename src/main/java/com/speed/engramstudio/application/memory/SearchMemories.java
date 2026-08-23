package com.speed.engramstudio.application.memory;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.repository.ObservationRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SearchMemories {

    private final ObservationRepository repository;

    public SearchMemories(ObservationRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<List<Observation>> execute(String query) {
        return repository.search(query);
    }
}
