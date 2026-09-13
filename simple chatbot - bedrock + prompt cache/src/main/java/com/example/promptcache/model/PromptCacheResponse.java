package com.example.promptcache.model;

public record PromptCacheResponse(String answer, int inputTokens, int outputTokens,
                                Integer cacheWriteInputTokens, Integer cacheReadInputTokens, long durationMs) {
}
