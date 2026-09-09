package com.example.petstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class PetStoreAgentLambdaApplication {

    public static void main(String[] args) {
        SpringApplication.run(PetStoreAgentLambdaApplication.class, args);
    }
}
