package com.example.promptcache.controller;

import com.example.promptcache.model.PromptCacheComparisonResponse;
import com.example.promptcache.model.PromptCacheResponse;
import com.example.promptcache.service.PromptCacheService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/cache-demo", produces = MediaType.APPLICATION_JSON_VALUE)
public class PromptCacheController {
    private final PromptCacheService service;

    public PromptCacheController(PromptCacheService service) {
        this.service = service;
    }

    @GetMapping
    public PromptCacheResponse ask(@RequestParam String question) {
        return service.ask(question);
    }

    @GetMapping("/compare")
    public PromptCacheComparisonResponse compare() {
        PromptCacheResponse first = service.ask("Summarize the architecture described in the document.");
        PromptCacheResponse second = service.ask("What scalability risks are described in the document?");
        return new PromptCacheComparisonResponse(first, second);
    }
}
