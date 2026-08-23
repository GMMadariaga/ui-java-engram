package com.speed.engramstudio.application.prompts;

import com.speed.engramstudio.domain.model.Prompt;
import com.speed.engramstudio.domain.repository.PromptRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GetRecentPrompts {

    private final PromptRepository repository;

    public GetRecentPrompts(PromptRepository repository) {
        this.repository = repository;
    }

    public CompletableFuture<List<Prompt>> execute() {
        return repository.getRecent();
    }
}
