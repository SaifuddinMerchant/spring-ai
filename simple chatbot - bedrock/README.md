# Game Expert Chatbot

Game Expert Chatbot is a Spring Boot and Spring AI application that answers questions about board, card, tabletop, party, and video games. It can teach rules, clarify disputes, suggest legal moves and strategies, explain setup and scoring, compare games, and recommend games for a particular group.

The application combines an Amazon Bedrock chat model with a game-focused system prompt and conversation memory. It provides both a browser-based interface and a REST API.

## How it works

Every request passes through a Spring AI `ChatClient` configured with these components:

1. **System prompt** — `src/main/resources/prompts/game-expert-system-prompt.txt` defines the assistant as a friendly game expert. It limits answers to games and closely related subjects, asks the model to distinguish official rules from house rules, and instructs it not to invent details when editions differ or information is uncertain.
2. **Conversation memory** — `MessageChatMemoryAdvisor` maintains a window of the latest 20 messages for each `conversationId`. Reusing an ID gives follow-up questions conversational context. Memory is held in-process and is cleared when the application restarts.

The resulting request flow is:

```text
User question
    -> system prompt + conversation history
    -> Amazon Bedrock chat model
    -> answer
```

## Prerequisites

- Java 21
- Maven 3.9 or later
- An AWS account with access to Amazon Bedrock

See [AWS setup](docs/aws-setup.md) for IAM permissions, local credentials, model configuration, and AWS-specific startup instructions. Amazon Bedrock usage may incur charges.

## Run the application

After completing the AWS setup, start the application:

```bash
mvn spring-boot:run
```

Open `http://localhost:8080` for the game-focused web interface. It is served directly by Spring Boot and requires no separate frontend build.

## Call the chatbot

You can also call the REST API directly:

```bash
curl -X POST http://localhost:8080/api/chat \
  -H "Content-Type: application/json" \
  -d '{"prompt":"Teach me how to play Splendor.","conversationId":"demo-conversation"}'
```

Example response:

```json
{
  "response": "In Splendor, you collect gem tokens to buy development cards..."
}
```

The Actuator health endpoint is available at `GET /actuator/health`.

Send the same `conversationId` in later requests to continue that conversation. Use a different ID to begin an independent conversation.

## Deploy as an AWS Lambda Function URL

`mvn package` also produces `target/simple-chatbot-0.0.1-SNAPSHOT-lambda.jar`, an AWS Lambda deployment artifact. Upload that JAR to a Java 21 Lambda function, configure the handler as `com.example.simplechatbot.lambda.ChatLambdaHandler::handleRequest`, and create a Function URL.

The Function URL accepts a `POST` request with the same JSON payload as the REST service and returns the same `{"response":"..."}` body. Configure its execution role with the Bedrock permissions described in [AWS setup](docs/aws-setup.md). The static page remains served by the Spring Boot application and continues to call `/api/chat`.

## Build and test

The default test suite uses mocked AI components and does not require live AWS credentials:

```bash
mvn clean verify
```
