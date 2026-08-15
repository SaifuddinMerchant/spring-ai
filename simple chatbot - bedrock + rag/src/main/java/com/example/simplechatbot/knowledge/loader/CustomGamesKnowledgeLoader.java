package com.example.simplechatbot.knowledge.loader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomGamesKnowledgeLoader implements ApplicationRunner {

    private final VectorStore vectorStore;
    private final ResourcePatternResolver resourceResolver;

    @Override
    public void run(ApplicationArguments args) {
        try {
            load(Arrays.asList(resourceResolver.getResources("classpath*:custom-games/*.md")));
        } catch (IOException exception) {
            throw new IllegalStateException("Could not find custom game rules", exception);
        }
    }

    void load(List<Resource> markdownFiles) {
        List<Document> ruleDocuments = markdownFiles.stream()
                .sorted(Comparator.comparing(this::filename))
                .flatMap(resource -> readMarkdown(resource).stream())
                .toList();

        if (ruleDocuments.isEmpty()) {
            log.info("No custom game Markdown files found on the classpath");
            return;
        }

        List<Document> chunks = TokenTextSplitter.builder()
                .withChunkSize(800)
                .withMinChunkSizeChars(350)
                .withMinChunkLengthToEmbed(10)
                .withKeepSeparator(true)
                .build()
                .apply(ruleDocuments);

        vectorStore.add(chunks);
        log.info("Loaded {} custom game rule files as {} vector documents", markdownFiles.size(), chunks.size());
    }

    private List<Document> readMarkdown(Resource resource) {
        MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                .withHorizontalRuleCreateDocument(true)
                .withIncludeCodeBlock(false)
                .withIncludeBlockquote(false)
                .withAdditionalMetadata("source", filename(resource))
                .build();
        return new MarkdownDocumentReader(resource, config).read();
    }

    private String filename(Resource resource) {
        return resource.getFilename() != null ? resource.getFilename() : resource.getDescription();
    }
}
