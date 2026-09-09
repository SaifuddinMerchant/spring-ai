package com.example.petstore.config;

import java.net.URI;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.bedrockagentcore.BedrockAgentCoreClient;
import tools.jackson.databind.ObjectMapper;

@Configuration
@EnableConfigurationProperties(PetStoreAgentProperties.class)
class AgentCoreConfiguration {

    @Bean
    BedrockAgentCoreClient bedrockAgentCoreClient(PetStoreAgentProperties properties) {
        return BedrockAgentCoreClient.builder()
                .region(Region.of(properties.region()))
                .build();
    }

    @Bean
    ObjectMapper defaultObjectMapper(){
        return new ObjectMapper();
    }
}
