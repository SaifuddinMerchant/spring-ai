package com.example.simplechatbot.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String prompt, @JsonAlias("conversationId") @NotBlank String sessionId) {
}
