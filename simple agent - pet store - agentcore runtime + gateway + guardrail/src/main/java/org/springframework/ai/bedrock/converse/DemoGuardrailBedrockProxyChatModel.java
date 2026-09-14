package org.springframework.ai.bedrock.converse;

import io.micrometer.observation.ObservationRegistry;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import software.amazon.awssdk.services.bedrockruntime.model.ConverseRequest;
import software.amazon.awssdk.services.bedrockruntime.model.GuardrailConfiguration;

import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;

/**
 * Demo-only extension that adds an Amazon Bedrock guardrail to every Converse request.
 *
 * <p>Production code should prefer composition when Spring AI exposes request
 * customization for this use case. This subclass is in the Spring AI package only because
 * {@code BedrockProxyChatModel#createRequest(Prompt)} is package-private. See
 * <a href="https://github.com/spring-projects/spring-ai/issues/1184">Spring AI issue #1184</a>.
 */
public final class DemoGuardrailBedrockProxyChatModel extends BedrockProxyChatModel {

    private final GuardrailConfiguration guardrailConfiguration;

    public DemoGuardrailBedrockProxyChatModel(BedrockRuntimeClient bedrockRuntimeClient,
            BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient, BedrockChatOptions options,
            ObservationRegistry observationRegistry, ToolCallingManager toolCallingManager,
            String guardrailIdentifier, String guardrailVersion) {
        super(bedrockRuntimeClient, bedrockRuntimeAsyncClient, options, observationRegistry, toolCallingManager);
        this.guardrailConfiguration = GuardrailConfiguration.builder()
            .guardrailIdentifier(guardrailIdentifier)
            .guardrailVersion(guardrailVersion)
            .build();
    }

    @Override
    ConverseRequest createRequest(Prompt prompt) {
        return super.createRequest(prompt)
            .toBuilder()
            .guardrailConfig(this.guardrailConfiguration)
            .build();
    }

}
