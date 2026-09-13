# Amazon Nova prompt caching with Spring AI

This Spring Boot application demonstrates **Amazon Bedrock-managed** prompt caching with Amazon Nova Lite and Spring AI's Bedrock Converse integration. It contains no Redis, Caffeine, Spring cache abstraction, or application-side prompt cache.

## Prerequisites

Use Java 21 and Maven. Your AWS identity needs permission to call the Bedrock Converse API and access to the selected Nova model in the configured region.

The application uses the AWS SDK default credential provider chain. That includes common mechanisms such as environment variables, Java system properties, `~/.aws/credentials` profiles, web identity credentials, ECS task credentials, and EC2 instance roles. No AWS keys are configured in this project.

## Run

Configure the region and, optionally, the model settings. Defaults are `us-east-1`, `amazon.nova-lite-v1:0`, 400 output tokens, and 0.2 temperature.

```powershell
$env:AWS_REGION = "us-east-1"
$env:BEDROCK_MODEL_ID = "amazon.nova-lite-v1:0"
mvn clean package
mvn spring-boot:run
```

`AWS_REGION`, `BEDROCK_MODEL_ID`, `BEDROCK_MAX_TOKENS`, and `BEDROCK_TEMPERATURE` override the defaults in `application.yml`.

Try one request:

```bash
curl "http://localhost:8080/api/cache-demo?question=What%20are%20the%20main%20architectural%20decisions?"
```

Or make two requests in sequence using the identical static system context:

```bash
curl "http://localhost:8080/api/cache-demo/compare"
```

The system prompt is loaded once from `prompts/ecommerce-arcitecture-expert-system-prompt`. It contains a realistic architecture document exceeding Nova's minimum cacheable prompt size and is never modified between calls. The user question is a separate `UserMessage`, so it can change without changing the cached prefix.

## Reading cache metrics

`cacheWriteInputTokens` is the number of input tokens Bedrock reported as written to a new prompt-cache entry. A positive value commonly appears on the initial request.

`cacheReadInputTokens` is the number of input tokens Bedrock reported as read from an existing prompt-cache entry. A positive value commonly appears on a later request with the same cacheable system content, before Bedrock's cache TTL expires.

The comparison endpoint returns Bedrock's actual values for both calls. It does not assume that the second call is a hit: cache availability can depend on Bedrock, model access, cache lifetime, and request compatibility. If Bedrock omits a cache metric, the JSON field is `null` rather than a made-up value. `inputTokens`, `outputTokens`, and the cache metrics come from the Spring AI Bedrock Converse response metadata; `durationMs` measures the local call duration.

Bedrock owns cache storage and expiry. This application only requests a `SYSTEM_ONLY` cache point through `BedrockCacheOptions`; it stores no prompt-cache entries itself.
