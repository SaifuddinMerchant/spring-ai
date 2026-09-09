package com.example.petstore.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.example.petstore.PetStoreAgentLambdaApplication;
import com.example.petstore.dto.AnswerResponse;
import com.example.petstore.dto.QuestionRequest;
import com.example.petstore.service.PetStoreAgentService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

public class PetStoreLambdaHandler implements RequestHandler<QuestionRequest, AnswerResponse> {

    private final PetStoreAgentService agentService;

    public PetStoreLambdaHandler() {
        this(createApplicationContext());
    }

    PetStoreLambdaHandler(PetStoreAgentService agentService) {
        this.agentService = agentService;
    }

    private PetStoreLambdaHandler(ConfigurableApplicationContext applicationContext) {
        this(applicationContext.getBean(PetStoreAgentService.class));
    }

    @Override
    public AnswerResponse handleRequest(QuestionRequest input, Context context) {
        return new AnswerResponse(agentService.ask(input.question()));
    }

    private static ConfigurableApplicationContext createApplicationContext() {
        return SpringApplication.run(PetStoreAgentLambdaApplication.class);
    }
}
