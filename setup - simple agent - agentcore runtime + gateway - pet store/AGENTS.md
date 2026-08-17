# AGENTS.md

## Project Purpose

This project is a small Java application for setting up AWS infrastructure through direct AWS SDK calls.

The primary AWS area is Amazon Bedrock AgentCore, including:

- AgentCore Runtime
- AgentCore Gateway
- AgentCore Memory
- Supporting AWS services required by the setup, such as Amazon S3 and Amazon ECR

Keep the implementation intentionally small, direct, and easy to follow.

## Technology Stack

- Java 25
- Maven
- AWS SDK for Java 2.x
- Lombok
- IntelliJ IDEA

Prefer Java records over classes where they are a good fit, particularly for immutable configuration and data-transfer types. Use Lombok where it reduces boilerplate and a record is not appropriate.

## Project Organization

Organize the code around the AWS service layer rather than around generic architectural patterns.

A reasonable structure is:

```text
src/main/java/<base-package>/
  agentcore/
    runtime/
    gateway/
    memory/
  s3/
  ecr/
  config/
  app/
```

Each AWS service area should contain only the code needed to configure or create resources for that service.

Avoid introducing extra abstraction layers, framework-style architectures, or generalized infrastructure libraries unless they are clearly required by the implementation.

## AWS SDK Usage

Use AWS SDK for Java 2.x directly.

Create AWS resources by invoking the appropriate AWS SDK clients. Do not introduce AWS CDK, CloudFormation generation, Terraform, or other infrastructure-as-code mechanisms unless explicitly requested.

Use the AWS SDK default credentials provider chain. Never hard-code:

- AWS access keys
- AWS secret keys
- session tokens
- account credentials

Do not add custom credential-loading logic unless explicitly requested.

Use configuration to supply values such as:

- AWS Region
- resource names
- ARNs
- bucket names
- repository names
- AgentCore identifiers
- other environment-specific settings

## Configuration

Application and infrastructure settings should come from `.properties` or YAML files.

Keep environment-specific values outside Java source code wherever practical.

Prefer a small configuration model that maps clearly to the configuration file. Records are preferred for configuration objects when suitable.

Example locations:

```text
src/main/resources/application.properties
```

or

```text
src/main/resources/application.yaml
```

Do not introduce a configuration framework solely for loading configuration unless it materially simplifies the implementation.

## Implementation Guidance

Keep the code simple and explicit.

For each AWS service:

1. Create or obtain the required AWS SDK client.
2. Read the required values from configuration.
3. Build the SDK request objects.
4. Invoke the AWS SDK operation.
5. Return or expose only the resource information needed by subsequent setup steps.

Prefer straightforward SDK calls over generalized wrappers or reusable frameworks.

Keep dependencies between AWS service areas visible. For example, if an AgentCore resource requires an S3 bucket or ECR repository, make that relationship clear in the orchestration code rather than hiding it behind unnecessary abstractions.

## Codex Instructions

When modifying this repository:

- Make only the changes necessary for the requested task.
- Keep the implementation small and focused on infrastructure setup.
- Use Java 25 language features where they improve clarity.
- Prefer records to regular classes when practical.
- Use Lombok when it meaningfully removes boilerplate and a record is not suitable.
- Use AWS SDK for Java 2.x APIs directly.
- Follow the AWS SDK default credentials provider chain.
- Read environment-specific values from `.properties` or YAML configuration rather than hard-coding them.
- Do not add tests unless explicitly requested.
- Do not attempt to run tests.
- Do not attempt to compile, build, execute, validate, or otherwise verify the code.
- Do not run Maven validation commands such as `mvn test`, `mvn verify`, `mvn package`, or `mvn compile` unless explicitly requested.
- Do not attempt to validate AWS connectivity, credentials, permissions, resource existence, or deployed infrastructure.
- Do not make AWS calls for verification purposes.
- Do not add validation-only scripts or temporary verification code.
- Assume the developer will manually review and validate all generated code.
- Do not add additional architectural layers, patterns, or frameworks unless the requested change clearly requires them.
- Do not add dependencies unless they are necessary for the requested implementation.
- Never place credentials or secrets in source code, configuration examples, logs, or generated files.

## Maven Dependencies

Use Maven for dependency management.

Prefer individual AWS SDK modules for the AWS services actually used rather than adding the entire AWS SDK when practical.

Expected dependency categories include:

- AWS SDK for Java 2.x modules required by AgentCore and supporting AWS services
- Lombok

Keep the `pom.xml` minimal and add new dependencies only when required by the implementation.

## Scope

Treat this as a simple infrastructure setup application.

Favor readable, direct code over extensibility. Avoid designing for hypothetical future requirements. Add structure only when the current infrastructure setup requires it.
