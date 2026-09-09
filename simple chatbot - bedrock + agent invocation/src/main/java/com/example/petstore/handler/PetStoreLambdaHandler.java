package com.example.petstore.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.example.petstore.PetStoreAgentLambdaApplication;
import com.example.petstore.dto.AnswerResponse;
import com.example.petstore.dto.QuestionRequest;
import com.example.petstore.service.LambdaRequestResponseService;
import com.example.petstore.service.PetStoreAgentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

@Slf4j
public class PetStoreLambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    private static final ConfigurableApplicationContext APPLICATION_CONTEXT =
            new SpringApplicationBuilder(PetStoreAgentLambdaApplication.class)
                    .web(WebApplicationType.NONE)
                    .run();

    private final PetStoreAgentService agentService;
    private final LambdaRequestResponseService requestResponseService;

    public PetStoreLambdaHandler() {
        this(APPLICATION_CONTEXT.getBean(PetStoreAgentService.class), APPLICATION_CONTEXT.getBean(LambdaRequestResponseService.class));

    }

    PetStoreLambdaHandler(PetStoreAgentService agentService, LambdaRequestResponseService lambdaRequestResponseService) {
        this.agentService = agentService;
        this.requestResponseService = lambdaRequestResponseService;
    }


    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        if (!"POST".equalsIgnoreCase(event.getRequestContext().getHttp().getMethod())) {
            return requestResponseService.response(405, Map.of("message", "Only POST is supported."));
        }

        try {
            QuestionRequest request = requestResponseService.requestBody(event, QuestionRequest.class);
            return requestResponseService.response(200, new AnswerResponse(agentService.ask(request.question())));
        } catch (Exception exception) {
            log.error("Error when handling lambda request", exception);
            return requestResponseService.response(500, Map.of("message", "The pet store agent could not answer right now."));
        }
    }
}
