package com.example.agentcore.agent;

import com.example.agentcore.request.InvocationRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springaicommunity.agentcore.annotation.AgentCoreInvocation;
import org.springframework.stereotype.Service;

@Service
public class QuestionAnswerAgent {

    private final ChatClient chatClient;

    public QuestionAnswerAgent(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @AgentCoreInvocation
    public String answer(InvocationRequest request) {
        return chatClient.prompt()
                .user(request.prompt())
                .advisors(advisor -> advisor.param(
                        ChatMemory.CONVERSATION_ID,
                        request.sessionId()))
                .call()
                .content();
    }
}
