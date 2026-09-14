mvn clean package

$Region = "us-east-1"
$Repository = "spring-ai-agent"

$AccountId = aws sts get-caller-identity --query Account --output text
$ImageUri = "$AccountId.dkr.ecr.$Region.amazonaws.com/${Repository}:latest"

aws ecr get-login-password --region $Region |
    docker login `
        --username AWS `
        --password-stdin "$AccountId.dkr.ecr.$Region.amazonaws.com"

docker buildx build `
    --platform linux/arm64 `
    --tag $ImageUri `
    --push `
    .
