package com.example.petstore.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("pet-store-agent")
public record PetStoreAgentProperties(
        String runtimeArn,
        String region,
        String endpointOverride) {
}
