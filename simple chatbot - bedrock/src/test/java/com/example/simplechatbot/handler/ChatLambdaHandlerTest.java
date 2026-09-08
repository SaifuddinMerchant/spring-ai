package com.example.simplechatbot.handler;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Http;
import com.example.simplechatbot.service.ChatService;
import com.example.simplechatbot.service.LambdaRequestResponseService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ChatLambdaHandlerTest {

    @Test
    void shouldDelegateFunctionUrlRequestToChatService() {
        ChatService chatService = mock(ChatService.class);
        when(chatService.chat("How do I play Splendor?", "session-123")).thenReturn("Collect gems first.");
        ChatLambdaHandler handler = new ChatLambdaHandler(
                chatService,
                new LambdaRequestResponseService(new ObjectMapper()),
                Validation.buildDefaultValidatorFactory().getValidator());

        var response = handler.handleRequest(postEvent("{\"prompt\":\"How do I play Splendor?\",\"sessionId\":\"session-123\"}"), null);

        assertThat(response.getStatusCode()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("{\"response\":\"Collect gems first.\"}");
        verify(chatService).chat("How do I play Splendor?", "session-123");
    }

    @Test
    void shouldReturnBadRequestForMissingRequiredFields() {
        ChatLambdaHandler handler = new ChatLambdaHandler(
                mock(ChatService.class),
                new LambdaRequestResponseService(new ObjectMapper()),
                Validation.buildDefaultValidatorFactory().getValidator());

        var response = handler.handleRequest(postEvent("{\"prompt\":\"\"}"), null);

        assertThat(response.getStatusCode()).isEqualTo(400);
        assertThat(response.getBody()).contains("prompt and sessionId are required.");
    }

    private APIGatewayV2HTTPEvent postEvent(String body) {
        var http = new Http();
        http.setMethod("POST");
        var requestContext = new RequestContext();
        requestContext.setHttp(http);
        var event = new APIGatewayV2HTTPEvent();
        event.setRequestContext(requestContext);
        event.setBody(body);
        return event;
    }
}
