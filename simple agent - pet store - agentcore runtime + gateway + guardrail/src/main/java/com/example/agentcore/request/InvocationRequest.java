package com.example.agentcore.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;
import java.util.UUID;

public record InvocationRequest(String prompt, @JsonProperty("session_id") String sessionId) {

    public InvocationRequest {
        Objects.requireNonNull(prompt, "prompt must not be null");
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }
    }
}
