# AWS setup

Game Expert Chatbot uses Amazon Bedrock for chat completion and text embeddings. By default it uses Amazon Nova 2 Lite (`us.amazon.nova-2-lite-v1:0`) for chat and Amazon Titan Text Embeddings V2 (`amazon.titan-embed-text-v2:0`) for the vector store, in `us-east-1`.

## Configure AWS IAM

Do not use the AWS account root user or put AWS keys in this repository. For local development, use IAM Identity Center (SSO) or an IAM user. When deploying to AWS, attach permissions to the application's workload role, such as an EC2 instance profile, ECS task role, or EKS service-account role.

The principal running the application must be allowed to invoke both the chat and embedding models. For a simple development setup, attach the AWS-managed `AmazonBedrockFullAccess` policy. For a restricted runtime policy, use permissions such as:

```json
{
  "Version": "2012-10-17",
  "Statement": [{
    "Sid": "InvokeBedrockModels",
    "Effect": "Allow",
    "Action": ["bedrock:InvokeModel", "bedrock:InvokeModelWithResponseStream"],
    "Resource": "*"
  }]
}
```

`Resource: "*"` keeps the example compatible with cross-Region inference profiles. In production, restrict access to the required inference-profile and foundation-model ARNs. See [Amazon Bedrock model access](https://docs.aws.amazon.com/bedrock/latest/userguide/model-access.html).

## Configure local credentials

The application uses the AWS SDK default credentials provider chain. The recommended local approach is IAM Identity Center:

```bash
aws configure sso --profile bedrock-dev
aws sso login --profile bedrock-dev
```

If your organization uses IAM access keys instead:

```bash
aws configure --profile bedrock-dev
```

Verify the credentials:

```bash
aws sts get-caller-identity --profile bedrock-dev
```

Never commit generated AWS configuration files or credentials. See the [AWS SDK for Java default credentials provider chain](https://docs.aws.amazon.com/sdk-for-java/latest/developer-guide/credentials-chain.html).

## Configure and run

PowerShell:

```powershell
$env:AWS_PROFILE = "bedrock-dev"
$env:AWS_REGION = "us-east-1"
$env:BEDROCK_MODEL_ID = "us.amazon.nova-2-lite-v1:0"
$env:BEDROCK_EMBEDDING_MODEL_ID = "amazon.titan-embed-text-v2:0"
mvn spring-boot:run
```

macOS or Linux:

```bash
export AWS_PROFILE=bedrock-dev
export AWS_REGION=us-east-1
export BEDROCK_MODEL_ID=us.amazon.nova-2-lite-v1:0
export BEDROCK_EMBEDDING_MODEL_ID=amazon.titan-embed-text-v2:0
mvn spring-boot:run
```

| Variable | Default | Purpose |
| --- | --- | --- |
| `AWS_REGION` | `us-east-1` | Region used by the Bedrock clients |
| `BEDROCK_MODEL_ID` | `us.amazon.nova-2-lite-v1:0` | Converse-compatible chat model or inference profile |
| `BEDROCK_EMBEDDING_MODEL_ID` | `amazon.titan-embed-text-v2:0` | Titan model used for rulebook embeddings |
| `BEDROCK_MAX_TOKENS` | `4096` | Maximum tokens in a generated response |

The selected models must be available through the configured Region, and the AWS identity must be permitted to invoke both.

[Return to the project README](../README.md)
