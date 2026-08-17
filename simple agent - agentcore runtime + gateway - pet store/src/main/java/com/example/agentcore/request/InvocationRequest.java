package com.example.agentcore.request;

import org.apache.commons.lang3.Validate;
import org.springframework.lang.NonNull;

public record InvocationRequest(@NonNull String prompt, @NonNull String conversationId) {

    public InvocationRequest {
        Validate.notBlank(prompt, "prompt must not be blank");
        Validate.notBlank(conversationId, "conversationId must not be blank");
    }
}
