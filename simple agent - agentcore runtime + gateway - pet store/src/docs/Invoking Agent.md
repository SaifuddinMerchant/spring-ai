# Invoking the Agent Runtime

## Bash

```bash
#!/usr/bin/env bash

# ============================================================
# Configuration
# ============================================================

ACCOUNT_ID="<aws-account-id>"
REGION="us-east-1"
RUNTIME_NAME="<runtime-name>"
QUALIFIER="DEFAULT"

# Message sent to the agent
MESSAGE="Hello Agent"

# Derived values
AGENT_RUNTIME_ARN="arn:aws:bedrock-agentcore:${REGION}:${ACCOUNT_ID}:runtime/${RUNTIME_NAME}"
LOG_GROUP="/aws/bedrock-agentcore/runtimes/${RUNTIME_NAME}-${QUALIFIER}"

TARGET_DIR="target"
PAYLOAD_FILE="${TARGET_DIR}/payload.json"
RESPONSE_FILE="${TARGET_DIR}/response.json"

# ============================================================
# Create Output Directory
# ============================================================

mkdir -p "$TARGET_DIR"

# ============================================================
# Generate Session ID
# ============================================================

SESSION_ID="$(uuidgen)-agentcore"

# ============================================================
# Create Request Payload
# ============================================================

printf '{"prompt":"%s","conversationId":"%s"}' "$MESSAGE" "$SESSION_ID" > "$PAYLOAD_FILE"

# ============================================================
# Invoke Agent
# ============================================================

aws bedrock-agentcore invoke-agent-runtime \
    --region "$REGION" \
    --agent-runtime-arn "$AGENT_RUNTIME_ARN" \
    --qualifier "$QUALIFIER" \
    --runtime-session-id "$SESSION_ID" \
    --content-type "application/json" \
    --accept "application/json" \
    --payload "fileb://$PAYLOAD_FILE" \
    "$RESPONSE_FILE"

echo "=== Agent Response ==="
cat "$RESPONSE_FILE"

# ============================================================
# Display Recent Runtime Logs
# ============================================================

echo
echo "=== Recent Runtime Logs ==="

aws logs tail \
    "$LOG_GROUP" \
    --region "$REGION" \
    --since 30m \
    --format short
```

---

## PowerShell

```powershell
# ============================================================
# Configuration
# ============================================================

$accountId = "<aws-account-id>"
$region = "us-east-1"
$runtimeName = "<runtime-name>"
$qualifier = "DEFAULT"

# Message sent to the agent
$message = "Hello Agent"

# Derived values
$agentRuntimeArn = "arn:aws:bedrock-agentcore:$region`:$accountId`:runtime/$runtimeName"
$logGroup = "/aws/bedrock-agentcore/runtimes/$runtimeName-$qualifier"

$targetDir = "target"
$payloadFile = Join-Path $targetDir "payload.json"
$responseFile = Join-Path $targetDir "response.json"

# ============================================================
# Create Output Directory
# ============================================================

New-Item `
    -ItemType Directory `
    -Path $targetDir `
    -Force | Out-Null

# ============================================================
# Generate Session ID
# ============================================================

$sessionId = ([guid]::NewGuid().ToString() + "-agentcore")

# ============================================================
# Create Request Payload
# ============================================================

@{
    prompt = $message
    conversationId = $sessionId
} |
ConvertTo-Json |
Set-Content `
    -Path $payloadFile `
    -Encoding utf8

# ============================================================
# Invoke Agent
# ============================================================

aws bedrock-agentcore invoke-agent-runtime `
    --region $region `
    --agent-runtime-arn $agentRuntimeArn `
    --qualifier $qualifier `
    --runtime-session-id $sessionId `
    --content-type "application/json" `
    --accept "application/json" `
    --payload "fileb://$payloadFile" `
    $responseFile

Write-Host "=== Agent Response ==="

Get-Content $responseFile

# ============================================================
# Display Recent Runtime Logs
# ============================================================

Write-Host ""
Write-Host "=== Recent Runtime Logs ==="

aws logs tail `
    $logGroup `
    --region $region `
    --since 30m `
    --format short
```
