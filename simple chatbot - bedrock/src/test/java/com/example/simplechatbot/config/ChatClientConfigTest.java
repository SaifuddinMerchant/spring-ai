package com.example.simplechatbot.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatClientConfigTest {

    @Test
    void shouldBuildChatClientWithGameExpertSystemPrompt() throws IOException {
        ChatClient.Builder builder = mock(ChatClient.Builder.class);
        ChatClient chatClient = mock(ChatClient.class);
        Resource systemPrompt = new ClassPathResource("prompts/game-expert-system-prompt.txt");
        ChatMemory chatMemory = new ChatClientConfig().chatMemory();
        when(builder.defaultSystem(systemPrompt)).thenReturn(builder);
        when(builder.defaultAdvisors(any(Advisor[].class))).thenReturn(builder);
        when(builder.build()).thenReturn(chatClient);

        ChatClient configuredClient = new ChatClientConfig().chatClient(builder, chatMemory, systemPrompt);

        assertThat(configuredClient).isSameAs(chatClient);
        assertThat(systemPrompt.getContentAsString(StandardCharsets.UTF_8))
                .contains("UNO", "Splendor", "strategy game for two people")
                .contains("Do not answer requests unrelated to games");
        verify(builder).defaultSystem(systemPrompt);
        verify(builder).defaultAdvisors(any(Advisor[].class));
        verify(builder).build();
    }
}
