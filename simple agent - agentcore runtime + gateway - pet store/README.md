# Spring AI AgentCore Pet Store Agent

A pet store assistant for Amazon Bedrock AgentCore Runtime. It uses Spring AI's `ChatClient` with the Amazon Bedrock Converse API and discovers pet-store tools through an Amazon Bedrock AgentCore Gateway MCP endpoint.

- AWS Region: `us-east-1`
- Model: `us.amazon.nova-2-lite-v1:0`
-
## Prerequisites

- Java 21
- Maven 3.9+
- AWS credentials with permission to invoke the configured Bedrock model and AgentCore gateway
- Model access enabled in Amazon Bedrock
- Docker with `buildx` for an AgentCore deployment image

The application uses the AWS default credentials provider chain. Locally, configure credentials with the AWS CLI or environment variables. In AgentCore Runtime, assign permissions to the runtime execution role; do not put access keys in the image.

## Run locally

```bash
mvn spring-boot:run
```

You can override the gateway base URL with `PET_STORE_GATEWAY_URL`.

Open `http://localhost:8080` to use the built-in pet store chat page.

Using `curl`:

```bash
curl http://localhost:8080/ping
curl -X POST http://localhost:8080/invocations \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Which dogs are currently available?","conversationId":"demo-conversation"}'
```

Using PowerShell:

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/ping"

$body = @{
    prompt = "Which dogs are currently available?"
    conversationId = "demo-conversation"
} | ConvertTo-Json

Invoke-RestMethod `
    -Uri "http://localhost:8080/invocations" `
    -Method Post `
    -ContentType "application/json" `
    -Body $body
```

Expected response shape:

```json
"..."
```

Requests with the same `conversationId` share a bounded, in-memory conversation context. The memory is cleared when the application restarts. The built-in chat page creates a separate conversation ID for each browser tab and retains it for that tab's session.

The AgentCore runtime starter supplies the `/invocations` and `/ping` endpoints. The
`@AgentCoreInvocation` method on `QuestionAnswerAgent` handles invocation requests.

Override the defaults with `AWS_REGION` and `BEDROCK_MODEL_ID`.

## Test

```bash
mvn test
```

The web-contract tests mock the LLM call, so they do not require AWS credentials.

## Build for AgentCore Runtime

AgentCore Runtime requires a Linux ARM64 image that listens on `0.0.0.0:8080` and exposes `POST /invocations` and `GET /ping`. The included Dockerfile meets those requirements.

```bash
docker buildx build --platform linux/arm64 -t agentcore-qa:latest --load .
```

Tag and push the image to Amazon ECR, then create an AgentCore Runtime using that ECR image and an execution role. The role needs `bedrock:InvokeModel` and `bedrock:InvokeModelWithResponseStream` for the selected model or inference profile. Configure the runtime protocol as HTTP.

## Build and push to Amazon ECR

From the project root, run the included PowerShell script:

```powershell
.\scripts\build-push-image-to-ecr.ps1
```

The script performs these actions:

1. Runs `mvn clean package` to build the application JAR.
2. Uses the AWS CLI to obtain the current AWS account ID.
3. Authenticates Docker with Amazon ECR in `us-east-1`.
4. Builds a Linux ARM64 image for AgentCore Runtime.
5. Tags the image as `<account-id>.dkr.ecr.us-east-1.amazonaws.com/spring-ai-agent:latest`.
6. Pushes the image to the `spring-ai-agent` ECR repository.

The ECR repository must already exist, and the active AWS credentials must allow ECR authentication and image pushes. Docker must be running with `buildx` support.
