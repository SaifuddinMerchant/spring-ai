package com.example.simplechatbot.dto;

import jakarta.validation.constraints.NotBlank;

public record ChatRequest(@NotBlank String prompt, @NotBlank String conversationId) {
}
