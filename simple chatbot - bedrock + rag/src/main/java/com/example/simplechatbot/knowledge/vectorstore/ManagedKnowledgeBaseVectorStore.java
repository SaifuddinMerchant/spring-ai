package com.example.simplechatbot.knowledge.vectorstore;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.bedrockagentruntime.BedrockAgentRuntimeClient;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Primary
@Component
@Slf4j
public class ManagedKnowledgeBaseVectorStore implements VectorStore {

    private final BedrockAgentRuntimeClient client;
    private final String knowledgeBaseId;
    private final int topK;

    public ManagedKnowledgeBaseVectorStore(
            BedrockAgentRuntimeClient client,
            @Value("${spring.ai.vectorstore.bedrock-knowledge-base.knowledge-base-id}") String knowledgeBaseId,
            @Value("${spring.ai.vectorstore.bedrock-knowledge-base.top-k}") int topK) {
        this.client = client;
        this.knowledgeBaseId = knowledgeBaseId;
        this.topK = topK;
    }

    @Override
    public List<Document> similaritySearch(SearchRequest searchRequest) {
        log.info("Search Request is {}, topK is {}", searchRequest, topK);

        var response = client.retrieve(r -> r
                .knowledgeBaseId(knowledgeBaseId)
                .retrievalQuery(q -> q.text(searchRequest.getQuery()))
                .retrievalConfiguration(c ->
                        c.managedSearchConfiguration(m ->
                                m.numberOfResults(topK))));

        return response.retrievalResults().stream()
                .map(result -> Document.builder()
                        .text(result.content().text())
                        .score(result.score() == null
                                ? null
                                : result.score())
                        .metadata(unwrapMetadata(result.metadata()))
                        .build())
                .toList();
    }

    private Map<String, Object> unwrapMetadata(
            Map<String, software.amazon.awssdk.core.document.Document> metadata) {
        var unwrappedMetadata = new LinkedHashMap<String, Object>();
        metadata.forEach((key, value) ->
                unwrappedMetadata.put(key, value.unwrap()));
        return unwrappedMetadata;
    }

    @Override
    public void add(@NonNull List<Document> documents) {
        throw new UnsupportedOperationException(
                "Managed Bedrock Knowledge Base is read-only");
    }

    @Override
    public void delete(@NonNull List<String> idList) {
        throw new UnsupportedOperationException(
                "Managed Bedrock Knowledge Base is read-only");
    }

    @Override
    public void delete(Filter.@NonNull Expression filterExpression) {
        throw new UnsupportedOperationException(
                "Managed Bedrock Knowledge Base is read-only");

    }
}
