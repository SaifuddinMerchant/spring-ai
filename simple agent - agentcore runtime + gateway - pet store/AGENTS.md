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

## Package Organization

- Organize Java classes by technical layer rather than by functional domain or feature.
- Use top-level packages such as `controller`, `service`, `request`, `response`, `dto`, and `exception`.
- Place persistence types in appropriate layer-based packages such as `entity` and `repository`.
- Place application-wide configuration in a `config` package.
- Keep request and response models separate when they represent different API contracts.
- Do not introduce package-by-feature structures for new code unless explicitly requested.


## Testing

* Do not write any test for any of the functionality
