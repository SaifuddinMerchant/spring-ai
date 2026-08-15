# Game Expert Chatbot

Game Expert Chatbot is a Spring Boot and Spring AI application that answers questions about board, card, tabletop, party, and video games. It can teach rules, clarify disputes, suggest legal moves and strategies, explain setup and scoring, compare games, and recommend games for a particular group.

The application combines an Amazon Bedrock chat model with a system prompt, conversation memory, and retrieval-augmented generation (RAG) over bundled game rulebooks. It provides both a browser-based interface and a REST API.

## How it works

Every request passes through a Spring AI `ChatClient` configured with these components:

1. **System prompt** — `src/main/resources/prompts/game-expert-system-prompt.txt` defines the assistant as a friendly game expert. It limits answers to games and closely related subjects, asks the model to distinguish official rules from house rules, and instructs it not to invent details when editions differ or information is uncertain.
2. **Vector store** — at startup, Markdown files from `src/main/resources/custom-games` are read, split into token-sized chunks, embedded with Amazon Titan Text Embeddings V2, and placed in an in-memory `SimpleVectorStore`. The store is rebuilt on every application start and is not persisted.
3. **Question-and-answer retrieval** — Spring AI's `QuestionAnswerAdvisor` searches the vector store for rulebook passages relevant to the user's question and supplies that context to the chat model. This lets the expert answer questions about custom games whose rules are included with the application.
4. **Conversation memory** — `MessageChatMemoryAdvisor` maintains a window of the latest 20 messages for each `conversationId`. Reusing an ID gives follow-up questions conversational context. Memory is held in-process and is cleared when the application restarts.

The resulting request flow is:

```text
User question
    -> system prompt + conversation history
    -> relevant rulebook passages from the vector store
    -> Amazon Bedrock chat model
    -> answer
```

## Prerequisites

- Java 25
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

## Add custom game knowledge

Add each custom game's rules as a `.md` file under `src/main/resources/custom-games`. The loader processes all matching Markdown files when the application starts, so restart the application after adding or changing a rulebook.

The bundled examples are `Alchemists_Apotheosis_Rules.md` and `Realmweaver_Hexbound_Rules.md`. Rulebook headings and metadata help organize the retrieved material; code blocks and block quotes are excluded during loading. Because both the vector store and chat memory are in memory, no database is required and neither is retained across restarts.

## Build and test

The default test suite uses mocked AI components and does not require live AWS credentials:

```bash
mvn clean verify
```
