package com.example.simplechatbot.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatClient chatClient;

    public String chat(String prompt, String conversationId) {
        log.info("Sending prompt for conversation: {}", conversationId);

        String response = chatClient.prompt()
                .user(prompt)
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
                .call()
                .content();

        assert response != null;
        log.info("Received response from chat client: {}", StringUtils.truncate(response, 64));

        return response;
    }
}
