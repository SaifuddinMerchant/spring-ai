package com.example.petstore.service;

import com.example.petstore.config.PetStoreAgentProperties;
import java.util.UUID;

import com.example.petstore.dto.AgentPrompt;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeRequest;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class PetStoreAgentService {

    private static final String APPLICATION_JSON = "application/json";

    private final BedrockAgentCoreClient agentCoreClient;
    private final PetStoreAgentProperties properties;
    private final ObjectMapper objectMapper;

    public String ask(String question) {
        if (!StringUtils.hasText(question)) {
            throw new IllegalArgumentException("The input field 'question' must not be blank.");
        }

        var request = InvokeAgentRuntimeRequest.builder()
                .agentRuntimeArn(properties.runtimeArn())
                .runtimeSessionId(UUID.randomUUID().toString())
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .payload(SdkBytes.fromUtf8String(toAgentPayload(question)))
                .build();

        ResponseBytes<InvokeAgentRuntimeResponse> response = agentCoreClient.invokeAgentRuntimeAsBytes(request);
        return response.asUtf8String();
    }

    private String toAgentPayload(String question) {
        try {
            return objectMapper.writeValueAsString(new AgentPrompt(question));
        }
        catch (JacksonException exception) {
            throw new IllegalStateException("Could not serialize the AgentCore request.", exception);
        }
    }
}
