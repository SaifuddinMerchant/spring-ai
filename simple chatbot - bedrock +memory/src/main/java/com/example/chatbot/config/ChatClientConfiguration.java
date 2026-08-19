package com.example.chatbot.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springaicommunity.agentcore.memory.longterm.AgentCoreMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class ChatClientConfiguration {

    @Bean
    ChatClient chatClient(
            ChatClient.Builder builder,
            AgentCoreMemory agentCoreMemory,
            @Value("classpath:prompt/system-prompt.txt") Resource systemPromptResource) throws IOException {

        String systemPrompt = systemPromptResource.getContentAsString(StandardCharsets.UTF_8);

        return builder
                .defaultSystem(systemPrompt)
                .defaultAdvisors(agentCoreMemory.advisors)
                .build();
    }
}
