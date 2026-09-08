package com.example.simplechatbot.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.example.simplechatbot.GameExpertChatbotApplication;
import com.example.simplechatbot.dto.ChatRequest;
import com.example.simplechatbot.dto.ChatResponse;
import com.example.simplechatbot.service.ChatService;
import com.example.simplechatbot.service.LambdaRequestResponseService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;
import java.util.Set;

@Slf4j
public class ChatLambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ConfigurableApplicationContext APPLICATION_CONTEXT =
            new SpringApplicationBuilder(GameExpertChatbotApplication.class)
                    .web(WebApplicationType.NONE)
                    .run();

    private final ChatService chatService;
    private final LambdaRequestResponseService requestResponseService;
    private final Validator validator;

    public ChatLambdaHandler() {
        this(APPLICATION_CONTEXT.getBean(ChatService.class),
                APPLICATION_CONTEXT.getBean(LambdaRequestResponseService.class),
                APPLICATION_CONTEXT.getBean(Validator.class));

    }

    ChatLambdaHandler(ChatService chatService, LambdaRequestResponseService requestResponseService, Validator validator) {
        this.chatService = chatService;
        this.requestResponseService = requestResponseService;
        this.validator = validator;
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        if (!"POST".equalsIgnoreCase(event.getRequestContext().getHttp().getMethod())) {
            return requestResponseService.response(405, Map.of("message", "Only POST is supported."));
        }

        try {
            ChatRequest request = requestResponseService.requestBody(event, ChatRequest.class);
            Set<ConstraintViolation<ChatRequest>> violations = validator.validate(request);
            if (!violations.isEmpty()) {
                return requestResponseService.response(400, Map.of("message", "prompt and sessionId are required."));
            }

            return requestResponseService.response(200, new ChatResponse(chatService.chat(request.prompt(), request.sessionId())));
        } catch (Exception exception) {
            log.error("Error when handling lambda request", exception);
            return requestResponseService.response(500, Map.of("message", "The game expert could not answer right now."));
        }
    }

}
