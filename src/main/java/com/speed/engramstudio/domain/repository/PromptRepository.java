package com.speed.engramstudio.domain.repository;

import com.speed.engramstudio.domain.model.Prompt;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PromptRepository {
    CompletableFuture<List<Prompt>> getRecent();
}
