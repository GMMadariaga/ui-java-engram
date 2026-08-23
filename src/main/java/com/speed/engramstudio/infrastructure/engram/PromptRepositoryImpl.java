package com.speed.engramstudio.infrastructure.engram;

import com.speed.engramstudio.domain.model.Prompt;
import com.speed.engramstudio.domain.repository.PromptRepository;
import com.speed.engramstudio.infrastructure.engram.api.PromptsApi;
import com.speed.engramstudio.infrastructure.engram.mapper.PromptMapper;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PromptRepositoryImpl implements PromptRepository {

    private final PromptsApi promptsApi;

    public PromptRepositoryImpl(PromptsApi promptsApi) {
        this.promptsApi = promptsApi;
    }

    @Override
    public CompletableFuture<List<Prompt>> getRecent() {
        return promptsApi.getRecent()
            .thenApply(PromptMapper::toDomain);
    }
}
