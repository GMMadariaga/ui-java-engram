package com.speed.engramstudio.infrastructure.engram;

import com.speed.engramstudio.domain.model.Observation;
import com.speed.engramstudio.domain.repository.ObservationRepository;
import com.speed.engramstudio.infrastructure.engram.api.ObservationsApi;
import com.speed.engramstudio.infrastructure.engram.api.SearchApi;
import com.speed.engramstudio.infrastructure.engram.mapper.ObservationMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ObservationRepositoryImpl implements ObservationRepository {

    private final ObservationsApi observationsApi;
    private final SearchApi searchApi;

    public ObservationRepositoryImpl(ObservationsApi observationsApi, SearchApi searchApi) {
        this.observationsApi = observationsApi;
        this.searchApi = searchApi;
    }

    @Override
    public CompletableFuture<List<Observation>> getAll() {
        return observationsApi.getAll()
            .thenApply(ObservationMapper::toDomain);
    }

    @Override
    public CompletableFuture<List<Observation>> getRecent() {
        return observationsApi.getRecent()
            .thenApply(ObservationMapper::toDomain);
    }

    @Override
    public CompletableFuture<Observation> getById(long id) {
        return observationsApi.getById(id)
            .thenApply(ObservationMapper::toDomain);
    }

    @Override
    public CompletableFuture<List<Observation>> search(String query) {
        if (query == null || query.isBlank()) {
            return getAll();
        }
        return searchApi.search(query)
            .thenApply(ObservationMapper::toDomain);
    }
}
