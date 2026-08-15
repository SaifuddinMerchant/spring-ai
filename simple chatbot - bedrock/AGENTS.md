# AGENTS.md

## Technology Stack

Use the following technologies unless explicitly instructed otherwise:

* Java 25
* Spring Boot 4.1.x
* Spring AI 2.x
* Spring Framework 7.x
* Maven
* Lombok
* JUnit 5
* Mockito
* AssertJ
* Testcontainers
* PostgreSQL (when persistence is required)
* Flyway (for database migrations)
* Spring Boot Actuator

## Coding Guidelines

* Use constructor injection.
* Prefer Java records for immutable DTOs.
* Prefer `@RequiredArgsConstructor` and `@Slf4j`.
* Avoid field injection.
* Keep controllers thin; place business logic in services.
* Store prompts under `src/main/resources/prompts`.
* Use Spring AI abstractions (`ChatClient`, Advisors, Tools) instead of provider-specific SDKs whenever possible.
* Keep provider-specific code isolated to configuration or adapter classes.
* Use `application.yaml` for configuration.
* Keep secrets in environment variables; never commit API keys.

## Testing

* Write unit tests for new functionality.
* Use Testcontainers for integration tests involving external services or databases.
* Do not require real AI provider credentials for the default test suite.

## Build

Before considering work complete:

```bash
./mvnw clean verify
```
