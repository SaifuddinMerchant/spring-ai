package com.example.promptcache.service;

import com.example.promptcache.configuration.PromptCacheProperties;
import com.example.promptcache.model.PromptCacheResponse;
import org.springframework.ai.bedrock.converse.BedrockChatOptions;
import org.springframework.ai.bedrock.converse.api.BedrockCacheOptions;
import org.springframework.ai.bedrock.converse.api.BedrockCacheStrategy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class PromptCacheService {
    private final ChatClient chatClient;

    public PromptCacheService(ChatModel chatModel, PromptCacheProperties properties,
                              @Value("classpath:prompts/ecommerce-arcitecture-expert-system-prompt") Resource contextResource) throws IOException {
        String systemContext = StreamUtils.copyToString(contextResource.getInputStream(), StandardCharsets.UTF_8);
        this.chatClient = ChatClient.builder(chatModel)
                .defaultSystem(systemContext)
                .defaultOptions(BedrockChatOptions.builder()
                        .model(properties.modelId())
                        .maxTokens(properties.maxTokens())
                        .temperature(properties.temperature())
                        .cacheOptions(BedrockCacheOptions.builder().strategy(BedrockCacheStrategy.SYSTEM_ONLY).build()))
                .build();
    }

    public PromptCacheResponse ask(String question) {
        long startedAt = System.nanoTime();
        ChatResponse response = chatClient.prompt().user(question).call().chatResponse();
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;
        return new PromptCacheResponse(response.getResult().getOutput().getText(),
                response.getMetadata().getUsage().getPromptTokens(),
                response.getMetadata().getUsage().getCompletionTokens(),
                metric(response, "cacheWriteInputTokens"), metric(response, "cacheReadInputTokens"), durationMs);
    }

    private Integer metric(ChatResponse response, String name) {
        Object value = response.getMetadata().get(name);
        return value instanceof Number number ? number.intValue() : null;
    }
}
