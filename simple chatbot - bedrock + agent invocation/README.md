# Pet Store Agent Lambda

This Java 21 Spring Boot Lambda forwards a Lambda event with a `question` field to the Pet Store AgentCore runtime and returns its response in an `answer` field.

## Lambda handler

Set the Lambda handler to:

```
com.example.petstore.handler.PetStoreLambdaHandler::handleRequest
```

Deploy `target/pet-store-agent-lambda-0.0.1-SNAPSHOT-lambda.jar` after running `./mvnw clean verify`.

## Event and response

```json
{"question":"Can you tell me more about this pet 16192485?"}
```

The response is:

```json
{"answer":"...agent response..."}
```

The Lambda execution role needs `bedrock-agentcore:InvokeAgentRuntime` for the configured runtime ARN. The ARN defaults to the supplied Pet Store runtime and may be overridden with `PET_STORE_AGENT_RUNTIME_ARN`.
