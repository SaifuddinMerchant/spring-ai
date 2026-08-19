# Spring AI AgentCore Memory Chatbot

A minimal Spring Boot 4.1 and Spring AI 2 chatbot that uses Amazon Bedrock Converse for responses and Amazon Bedrock AgentCore Memory for persistent short-term conversation history.

Each conversation ID uses AgentCore's `actorId:sessionId` format. Reusing both values lets the model recall recent messages; changing the session ID starts a separate conversation.

## Prerequisites

- Java 25
- Maven 3.9+
- AWS credentials available through the standard AWS credential provider chain
- Access to the configured Amazon Bedrock model
- An Amazon Bedrock AgentCore Memory resource
- IAM permissions for `bedrock-agentcore:ListEvents`, `bedrock-agentcore:CreateEvent`, and `bedrock-agentcore:DeleteEvent`

## Configuration

Set these environment variables:

```powershell
$env:AWS_REGION = "us-east-1"
$env:AGENTCORE_MEMORY_ID = "your-memory-id"
$env:BEDROCK_MODEL_ID = "anthropic.claude-3-5-sonnet-20240620-v1:0"
```

The project tracks the Spring AI 2-compatible `1.1.0-SNAPSHOT` line of `spring-ai-agentcore`. Its snapshot repository is already declared in `pom.xml`.

## Run

```powershell
mvn spring-boot:run
```

Open `http://localhost:8080`, or call the API directly:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"prompt":"My name is Sam.","actorId":"user-1","sessionId":"chat-1"}'
```

Send another request with the same `actorId` and `sessionId` to demonstrate memory:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"prompt":"What is my name?","actorId":"user-1","sessionId":"chat-1"}'
```

The health endpoint is available at `GET /actuator/health`.

## Request and response

Request:

```json
{
  "prompt": "What did I just tell you?",
  "actorId": "user-1",
  "sessionId": "chat-1"
}
```

Response:

```json
{
  "response": "You told me that your name is Sam."
}
```
