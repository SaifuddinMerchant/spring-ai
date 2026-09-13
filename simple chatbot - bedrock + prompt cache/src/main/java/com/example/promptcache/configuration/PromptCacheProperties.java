package com.example.promptcache.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("prompt-cache")
public record PromptCacheProperties(String modelId, Integer maxTokens, Double temperature) {
}
