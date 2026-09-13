package com.example.agentcore.config;

import io.micrometer.observation.ObservationRegistry;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProvider;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeAsyncClient;
import software.amazon.awssdk.services.bedrockruntime.BedrockRuntimeClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.observation.ChatModelObservationConvention;
import org.springframework.ai.model.bedrock.autoconfigure.BedrockAwsConnectionProperties;
import org.springframework.ai.model.bedrock.converse.autoconfigure.BedrockConverseProxyChatProperties;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.ai.bedrock.converse.DemoGuardrailBedrockProxyChatModel;

@Configuration
public class ChatClientConfig {

    @Bean
    ChatMemory chatMemory() {
        return MessageWindowChatMemory.builder()
                .maxMessages(40)
                .build();
    }

    @Bean
    ChatClient chatClient(
            DemoGuardrailBedrockProxyChatModel chatModel,
            ToolCallbackProvider toolCallbackProvider,
            ChatMemory chatMemory,
            @Value("classpath:/prompts/pet-store-agent-system.st") Resource systemPrompt) {
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultTools(toolCallbackProvider)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
    }

    @Bean
    BedrockRuntimeClient bedrockRuntimeClient(AwsCredentialsProvider credentialsProvider,
            AwsRegionProvider regionProvider, BedrockAwsConnectionProperties connectionProperties) {
        return BedrockRuntimeClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(regionProvider.getRegion())
                .httpClientBuilder(ApacheHttpClient.builder()
                        .connectionAcquisitionTimeout(connectionProperties.getConnectionAcquisitionTimeout())
                        .connectionTimeout(connectionProperties.getConnectionTimeout())
                        .socketTimeout(connectionProperties.getSocketTimeout()))
                .overrideConfiguration(configuration -> configuration.apiCallTimeout(connectionProperties.getTimeout()))
                .build();
    }

    @Bean
    BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient(AwsCredentialsProvider credentialsProvider,
            AwsRegionProvider regionProvider, BedrockAwsConnectionProperties connectionProperties) {
        return BedrockRuntimeAsyncClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(regionProvider.getRegion())
                .httpClientBuilder(NettyNioAsyncHttpClient.builder()
                        .tcpKeepAlive(true)
                        .readTimeout(connectionProperties.getAsyncReadTimeout())
                        .connectionTimeout(connectionProperties.getConnectionTimeout())
                        .connectionAcquisitionTimeout(connectionProperties.getConnectionAcquisitionTimeout())
                        .maxConcurrency(200))
                .overrideConfiguration(configuration -> configuration.apiCallTimeout(connectionProperties.getTimeout()))
                .build();
    }

    @Bean
    DemoGuardrailBedrockProxyChatModel guardrailBedrockProxyChatModel(BedrockRuntimeClient bedrockRuntimeClient,
            BedrockRuntimeAsyncClient bedrockRuntimeAsyncClient, BedrockConverseProxyChatProperties chatProperties,
            ToolCallingManager toolCallingManager, ObjectProvider<ObservationRegistry> observationRegistry,
            ObjectProvider<ChatModelObservationConvention> observationConvention,
            @Value("${app.bedrock.guardrail.identifier}") String guardrailIdentifier,
            @Value("${app.bedrock.guardrail.version}") String guardrailVersion) {
        var chatModel = new DemoGuardrailBedrockProxyChatModel(bedrockRuntimeClient, bedrockRuntimeAsyncClient,
                chatProperties.toOptions(), observationRegistry.getIfUnique(() -> ObservationRegistry.NOOP),
                toolCallingManager, guardrailIdentifier, guardrailVersion);
        observationConvention.ifAvailable(chatModel::setObservationConvention);
        return chatModel;
    }
}
