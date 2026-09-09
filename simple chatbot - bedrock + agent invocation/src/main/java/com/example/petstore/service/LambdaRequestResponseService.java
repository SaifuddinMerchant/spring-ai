package com.example.petstore.service;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

@Service
public class LambdaRequestResponseService {

    private static final Map<String, String> JSON_HEADERS = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*");

    private final ObjectMapper objectMapper;

    public LambdaRequestResponseService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public  <T> T requestBody(APIGatewayV2HTTPEvent event, Class<T> valueType) {
        if (event.getBody() == null) {
            return null;
        }

        String value =  event.getIsBase64Encoded()
                ? new String(Base64.getDecoder().decode(event.getBody()))
                : event.getBody();

        return objectMapper.readValue(value, valueType);
    }

    public APIGatewayV2HTTPResponse response(int statusCode, Object body) {
        return APIGatewayV2HTTPResponse.builder()
                .withStatusCode(statusCode)
                .withHeaders(JSON_HEADERS)
                .withBody(objectMapper.writeValueAsString(body))
                .build();
    }
}
