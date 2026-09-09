package com.example.petstore.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.petstore.dto.AnswerResponse;
import com.example.petstore.dto.QuestionRequest;
import com.example.petstore.service.PetStoreAgentService;
import org.junit.jupiter.api.Test;

class PetStoreLambdaHandlerTest {

    @Test
    void delegatesTheQuestionAndReturnsTheAnswer() {
        var agentService = org.mockito.Mockito.mock(PetStoreAgentService.class);
        when(agentService.ask("Tell me about pet 16192485")).thenReturn("A friendly dog.");
        var handler = new PetStoreLambdaHandler(agentService);

        AnswerResponse response = handler.handleRequest(new QuestionRequest("Tell me about pet 16192485"), null);

        assertThat(response).isEqualTo(new AnswerResponse("A friendly dog."));
    }
}
