package com.example.agentcore.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(40)
                .build();
    }

    @Bean
    ChatClient chatClient(
            ChatClient.Builder builder,
            ToolCallbackProvider toolCallbackProvider,
            ChatMemory chatMemory,
            @Value("classpath:/prompts/pet-store-agent-system.st") Resource systemPrompt) {
        return builder
                .defaultSystem(systemPrompt)
                .defaultTools(toolCallbackProvider)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }
}
