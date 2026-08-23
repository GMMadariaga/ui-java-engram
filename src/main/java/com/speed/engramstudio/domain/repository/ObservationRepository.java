package com.speed.engramstudio.domain.repository;

import com.speed.engramstudio.domain.model.Observation;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ObservationRepository {
    CompletableFuture<List<Observation>> getAll();
    CompletableFuture<List<Observation>> getRecent();
    CompletableFuture<Observation> getById(long id);
    CompletableFuture<List<Observation>> search(String query);
}
