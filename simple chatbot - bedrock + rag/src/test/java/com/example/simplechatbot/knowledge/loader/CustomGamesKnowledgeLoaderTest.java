package com.example.simplechatbot.knowledge.loader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.document.Document;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePatternResolver;

import org.springframework.ai.vectorstore.VectorStore;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Comparator;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CustomGamesKnowledgeLoaderTest {

    Path directory;

    @BeforeEach
    void createTestDirectory() throws Exception {
        directory = Files.createDirectories(Path.of("target", "test-custom-games", UUID.randomUUID().toString()));
    }

    @AfterEach
    void removeTestDirectory() throws Exception {
        if (Files.notExists(directory)) {
            return;
        }
        try (var paths = Files.walk(directory)) {
            for (Path path : paths.sorted(Comparator.reverseOrder()).toList()) {
                Files.deleteIfExists(path);
            }
        }
    }

    @Test
    void shouldLoadMarkdownRulesUsingSpringAiEtlWithSourceMetadata() throws Exception {
        Files.writeString(directory.resolve("sky-castles.md"), "# Sky Castles\n\nCollect three clouds to win.");
        Files.writeString(directory.resolve("notes.txt"), "This is not a rules file.");
        VectorStore vectorStore = mock(VectorStore.class);
        ResourcePatternResolver resourceResolver = mock(ResourcePatternResolver.class);

        new CustomGamesKnowledgeLoader(vectorStore, resourceResolver)
                .load(List.of(new FileSystemResource(directory.resolve("sky-castles.md"))));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Document>> documents = ArgumentCaptor.forClass(List.class);
        verify(vectorStore).add(documents.capture());
        assertThat(documents.getValue()).isNotEmpty().allSatisfy(document -> {
            assertThat(document.getText()).contains("Collect three clouds");
            assertThat(document.getMetadata()).containsEntry("source", "sky-castles.md");
        });
    }

    @Test
    void shouldNotCallEmbeddingStoreWhenNoRulesAreFound() {
        VectorStore vectorStore = mock(VectorStore.class);
        ResourcePatternResolver resourceResolver = mock(ResourcePatternResolver.class);

        new CustomGamesKnowledgeLoader(vectorStore, resourceResolver).load(List.of());

        verifyNoInteractions(vectorStore);
    }
}
