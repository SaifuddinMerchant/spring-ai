package com.example.simplechatbot.controller;

import com.example.simplechatbot.dto.ChatRequest;
import com.example.simplechatbot.dto.ChatResponse;
import com.example.simplechatbot.service.ChatService;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChatControllerTest {

    @Test
    void shouldReturnChatResponse() {
        ChatService chatService = mock(ChatService.class);
        
        when(chatService.chat("Hello", "conversation-123")).thenReturn("Hi there!");

        ChatController controller = new ChatController(chatService);
        ChatResponse response = controller.chat(new ChatRequest("Hello", "conversation-123"));

        assertThat(response.response()).isEqualTo("Hi there!");
    }
}
