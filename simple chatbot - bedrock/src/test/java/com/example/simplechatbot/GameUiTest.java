package com.example.simplechatbot;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class GameUiTest {

    @Test
    void shouldProvideFrameworkFreeGameChatInterface() throws IOException {
        var page = new ClassPathResource("static/index.html");

        assertThat(page.exists()).isTrue();
        assertThat(page.getContentAsString(StandardCharsets.UTF_8))
                .contains("Game Guide", "Suggest a strategy game for 2 people")
                .contains("fetch('/api/chat'")
                .contains("sessionStorage", "conversationId")
                .doesNotContain("<script src=", "<link rel=\"stylesheet\"");
    }
}
