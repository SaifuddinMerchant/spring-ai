package com.example.chatbot.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;

    public String chat(String prompt, String actorId, String sessionId) {

        return chatClient.prompt()
                .user(prompt)
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        actorId + ":" + sessionId))
                .call()
                .content();
    }
}
