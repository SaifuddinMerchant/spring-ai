# Amazon Bedrock AgentCore Harness Quickstart

This repository is a quickstart for creating and deploying an AI agent with the
[Amazon Bedrock AgentCore managed harness](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/harness.html).
A harness is configuration-based: you choose the model, instructions, tools,
memory, and runtime settings, while AgentCore manages the agent loop and its
infrastructure. No agent framework or orchestration code is required.

## Prerequisites

Before starting, install or obtain:

- An AWS account and an AWS identity with permission to deploy AgentCore and
  AWS CDK resources. See the
  [AgentCore IAM documentation](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/security-iam.html).
- [AWS CLI v2](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html)
  with credentials configured for an
  [AgentCore-supported Region](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/agentcore-regions.html).
- [Node.js 20 or later](https://nodejs.org/) and npm.

Check the local tools and AWS session:

```shell
node --version
npm --version
aws --version
aws sts get-caller-identity
```

If you use AWS IAM Identity Center, sign in before deployment:

```shell
aws sso login --profile <profile-name>
```

Then either set `AWS_PROFILE` in your shell or pass the appropriate profile
through your normal AWS configuration.

## Install the AgentCore CLI

Install the current AgentCore CLI globally with npm:

```shell
npm install -g @aws/agentcore
agentcore --version
```

To update it later, rerun the install command or run:

```shell
agentcore update
```

> **CLI name conflict:** The older Python package
> `bedrock-agentcore-starter-toolkit` also installs a command named
> `agentcore`. If the new CLI reports a conflict, uninstall the older package
> using the same Python package manager with which it was installed, for
> example `pip uninstall bedrock-agentcore-starter-toolkit`.

## Create a harness agent

From the directory in which you want the new project directory to be created,
start the interactive wizard:

```shell
agentcore create
```

In the wizard:

1. Enter a project name, such as `simple-agent-harness`.
2. Select **Harness** as the project type.
3. Select **Amazon Bedrock** as the model provider and choose a model.
4. Choose an environment. The default managed environment is sufficient for a
   basic agent.
5. Choose whether the harness should use memory.
6. Optionally configure tools, authentication, networking, lifecycle limits,
   truncation, and session storage.
7. Review the configuration and confirm creation.

The CLI creates a project directory containing `agentcore/agentcore.json` and
`agentcore/aws-targets.json`. Change into that generated directory before using
the remaining commands:

```shell
cd simple-agent-harness
```

For a non-interactive harness with default settings, use:

```shell
agentcore create --name simple-agent-harness --model-provider bedrock
cd simple-agent-harness
```

## Test the harness

Start the development experience:

```shell
agentcore dev
```

For a harness, this command first provisions its required AWS resources, then
starts a local server and opens the Agent Inspector. The inspector lets you chat
with the harness and inspect its traces and resources. This step therefore
requires valid AWS credentials and can create billable resources.

Use `agentcore dev --no-browser` for the terminal interface instead, or stop the
development server with `Ctrl+C`.

## Preview and deploy to AWS

Run these commands from the generated project directory.

Preview the infrastructure changes:

```shell
agentcore deploy --plan
```

Deploy the harness and its supporting infrastructure:

```shell
agentcore deploy
```

The first deployment can take several minutes because the CLI uses AWS CDK and
may need to bootstrap the account and Region. Check the resulting resources:

```shell
agentcore status
```

## Invoke the deployed harness

A session ID must be at least 33 characters. Reuse one session ID to continue a
conversation, or generate a new UUID for a new session.

macOS/Linux:

```shell
agentcore invoke --harness simple-agent-harness \
  --session-id "$(uuidgen)" \
  "Hello! What can you help me with?"
```

PowerShell:

```powershell
$sessionId = [guid]::NewGuid().ToString()
agentcore invoke --harness simple-agent-harness --session-id $sessionId "Hello! What can you help me with?"
```

Inspect the deployed agent when troubleshooting:

```shell
agentcore logs
agentcore traces list
```

## Iterate

Edit the harness definition in `agentcore/agentcore.json`, or use commands such
as `agentcore add harness`, `agentcore add tool`, and `agentcore add skill`.
After changing resources, deploy again:

```shell
agentcore deploy
```

## Clean up

AgentCore and its supporting AWS services can incur charges. When the project is
no longer needed, remove its resources from the project configuration and apply
that empty configuration to AWS:

```shell
agentcore remove all
agentcore deploy
```

Confirm in the command output and AWS console that the resources were removed.

## References

- [AgentCore harness getting started guide](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/harness-get-started.html)
- [AgentCore CLI getting started guide](https://docs.aws.amazon.com/bedrock-agentcore/latest/devguide/agentcore-get-started-cli.html)
- [AgentCore CLI source and command reference](https://github.com/aws/agentcore-cli)
