package com.example.petstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.petstore.config.PetStoreAgentProperties;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeRequest;
import software.amazon.awssdk.services.bedrockagentcore.model.InvokeAgentRuntimeResponse;
import tools.jackson.databind.ObjectMapper;

class PetStoreAgentServiceTest {

    private final BedrockAgentCoreClient agentCoreClient = org.mockito.Mockito.mock(BedrockAgentCoreClient.class);
    private final PetStoreAgentProperties properties = new PetStoreAgentProperties(
            "arn:aws:bedrock-agentcore:us-east-1:xyz:runtime/pet_store_agent-xyz",
            "us-east-1",
            null);
    private final PetStoreAgentService service = new PetStoreAgentService(
            agentCoreClient, properties, new ObjectMapper());

    @Test
    void sendsQuestionAsPromptAndReturnsAgentAnswer() {
        var agentResponse = InvokeAgentRuntimeResponse.builder().contentType("application/json").build();
        when(agentCoreClient.invokeAgentRuntimeAsBytes(any(InvokeAgentRuntimeRequest.class)))
                .thenReturn(ResponseBytes.fromByteArray(agentResponse, "pet details".getBytes(StandardCharsets.UTF_8)));

        String answer = service.ask("Can you tell me more about this pet 16192485?");

        assertThat(answer).isEqualTo("pet details");
        var requestCaptor = ArgumentCaptor.forClass(InvokeAgentRuntimeRequest.class);
        verify(agentCoreClient).invokeAgentRuntimeAsBytes(requestCaptor.capture());
        var request = requestCaptor.getValue();
        assertThat(request.agentRuntimeArn()).isEqualTo(properties.runtimeArn());
        assertThat(request.contentType()).isEqualTo("application/json");
        assertThat(request.accept()).isEqualTo("application/json");
        assertThat(request.payload().asUtf8String())
                .isEqualTo("{\"prompt\":\"Can you tell me more about this pet 16192485?\"}");
        assertThat(request.runtimeSessionId()).isNotBlank();
    }

    @Test
    void rejectsABlankQuestionBeforeCallingTheAgent() {
        assertThatIllegalArgumentException().isThrownBy(() -> service.ask(" "))
                .withMessage("The input field 'question' must not be blank.");
    }
}
