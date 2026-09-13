package com.example.promptcache.service;

import com.example.promptcache.configuration.PromptCacheProperties;
import com.example.promptcache.model.PromptCacheResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.bedrock.converse.BedrockChatOptions;
import org.springframework.ai.bedrock.converse.api.BedrockCacheStrategy;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PromptCacheServiceTest {

    @Test
    void usesAnIdenticalSystemMessageAndBedrockSystemOnlyCachingForEachRequest() throws Exception {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.getOptions()).thenReturn(BedrockChatOptions.builder().build());
        ChatResponse response = response();
        when(chatModel.call(org.mockito.ArgumentMatchers.any(Prompt.class))).thenReturn(response);
        PromptCacheService service = new PromptCacheService(chatModel,
                new PromptCacheProperties("amazon.nova-lite-v1:0", 400, 0.2),
                new ClassPathResource("prompts/ecommerce-arcitecture-expert-system-prompt"));

        service.ask("first question");
        service.ask("second question");

        ArgumentCaptor<Prompt> prompts = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel, org.mockito.Mockito.times(2)).call(prompts.capture());
        Prompt first = prompts.getAllValues().get(0);
        Prompt second = prompts.getAllValues().get(1);
        BedrockChatOptions options = (BedrockChatOptions) first.getOptions();

        assertThat(first.getInstructions().getFirst().getMessageType()).isEqualTo(MessageType.SYSTEM);
        assertThat(first.getInstructions().getFirst().getText()).isEqualTo(second.getInstructions().getFirst().getText());
        assertThat(first.getInstructions().get(1).getText()).isEqualTo("first question");
        assertThat(second.getInstructions().get(1).getText()).isEqualTo("second question");
        assertThat(options.getCacheOptions().getStrategy()).isEqualTo(BedrockCacheStrategy.SYSTEM_ONLY);
    }

    @Test
    void returnsBedrockUsageAndCacheMetadata() throws Exception {
        ChatModel chatModel = mock(ChatModel.class);
        when(chatModel.getOptions()).thenReturn(BedrockChatOptions.builder().build());
        ChatResponse response = response();
        when(chatModel.call(org.mockito.ArgumentMatchers.any(Prompt.class))).thenReturn(response);
        PromptCacheService service = new PromptCacheService(chatModel,
                new PromptCacheProperties("amazon.nova-lite-v1:0", 400, 0.2),
                new ClassPathResource("prompts/ecommerce-arcitecture-expert-system-prompt"));

        PromptCacheResponse result = service.ask("question");

        assertThat(result.answer()).isEqualTo("answer");
        assertThat(result.inputTokens()).isEqualTo(2100);
        assertThat(result.outputTokens()).isEqualTo(120);
        assertThat(result.cacheWriteInputTokens()).isEqualTo(2050);
        assertThat(result.cacheReadInputTokens()).isEqualTo(0);
        assertThat(result.durationMs()).isGreaterThanOrEqualTo(0);
    }

    private ChatResponse response() {
        ChatResponse response = mock(ChatResponse.class);
        Generation generation = mock(Generation.class);
        AssistantMessage message = mock(AssistantMessage.class);
        ChatResponseMetadata metadata = mock(ChatResponseMetadata.class);
        Usage usage = mock(Usage.class);
        when(response.getResult()).thenReturn(generation);
        when(generation.getOutput()).thenReturn(message);
        when(message.getText()).thenReturn("answer");
        when(response.getMetadata()).thenReturn(metadata);
        when(metadata.getUsage()).thenReturn(usage);
        when(usage.getPromptTokens()).thenReturn(2100);
        when(usage.getCompletionTokens()).thenReturn(120);
        when(metadata.get("cacheWriteInputTokens")).thenReturn(2050);
        when(metadata.get("cacheReadInputTokens")).thenReturn(0);
        return response;
    }
}
