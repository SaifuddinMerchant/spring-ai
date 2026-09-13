package com.example.promptcache;

import com.example.promptcache.configuration.PromptCacheProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PromptCacheProperties.class)
public class PromptCacheApplication {
    public static void main(String[] args) {
        SpringApplication.run(PromptCacheApplication.class, args);
    }
}
