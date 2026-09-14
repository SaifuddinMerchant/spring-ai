package merchant.saifuddin.example.setup.agentcore.gateway;

import merchant.saifuddin.example.setup.config.GatewayConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.*;

import java.time.Instant;

public final class AgentCoreGatewayManager {
    private static final String DESCRIPTION = "MCP gateway for the pet store REST API";
    private static final String TARGET_DESCRIPTION = "Pet store REST API from an S3 OpenAPI schema";

    private final BedrockAgentCoreControlClient agentCore;
    private final GatewayConfiguration configuration;

    public AgentCoreGatewayManager(BedrockAgentCoreControlClient agentCore,
                                   GatewayConfiguration configuration) {
        this.agentCore = agentCore;
        this.configuration = configuration;
    }

    public GatewayResult createOrUpdate(String roleArn) {
        var existing = findGateway();
        String gatewayId;

        if (existing == null) {
            gatewayId = agentCore.createGateway(CreateGatewayRequest.builder()
                    .name(configuration.gatewayName())
                    .description(DESCRIPTION)
                    .roleArn(roleArn)
                    .protocolType("MCP")
                    .authorizerType("NONE")
                    .build()).gatewayId();
        } else {
            gatewayId = existing.gatewayId();
            waitForGatewayStable(gatewayId);
            agentCore.updateGateway(UpdateGatewayRequest.builder()
                    .gatewayIdentifier(gatewayId)
                    .name(configuration.gatewayName())
                    .description(DESCRIPTION)
                    .roleArn(roleArn)
                    .protocolType("MCP")
                    .authorizerType("NONE")
                    .build());
        }

        var gateway = waitForGateway(gatewayId);
        createOrUpdateTarget(gatewayId);
        waitForTarget(gatewayId);
        return new GatewayResult(gateway.gatewayId(), gateway.gatewayArn(), gateway.gatewayUrl());
    }

    private GatewaySummary findGateway() {
        String nextToken = null;
        do {
            var response = agentCore.listGateways(ListGatewaysRequest.builder()
                    .maxResults(1000).nextToken(nextToken).build());
            var match = response.items().stream()
                    .filter(item -> configuration.gatewayName().equals(item.name())).findFirst();
            if (match.isPresent()) {
                return match.get();
            }
            nextToken = response.nextToken();
        } while (nextToken != null);
        return null;
    }

    private GetGatewayResponse waitForGateway(String gatewayId) {
        Instant deadline = Instant.now().plus(configuration.waitTimeout());
        while (Instant.now().isBefore(deadline)) {
            var gateway = agentCore.getGateway(GetGatewayRequest.builder()
                    .gatewayIdentifier(gatewayId).build());
            if ("READY".equals(gateway.statusAsString())) {
                return gateway;
            }
            if ("FAILED".equals(gateway.statusAsString())
                    || "UPDATE_UNSUCCESSFUL".equals(gateway.statusAsString())) {
                throw new IllegalStateException("Gateway did not become ready: " + gateway.statusReasons());
            }
            pause();
        }
        throw new IllegalStateException("Timed out waiting for gateway " + gatewayId);
    }

    private void waitForGatewayStable(String gatewayId) {
        Instant deadline = Instant.now().plus(configuration.waitTimeout());
        while (Instant.now().isBefore(deadline)) {
            String status = agentCore.getGateway(GetGatewayRequest.builder()
                    .gatewayIdentifier(gatewayId).build()).statusAsString();
            if ("READY".equals(status) || "FAILED".equals(status)
                    || "UPDATE_UNSUCCESSFUL".equals(status)) {
                return;
            }
            pause();
        }
        throw new IllegalStateException("Timed out waiting to update gateway " + gatewayId);
    }

    private void createOrUpdateTarget(String gatewayId) {
        var targetConfiguration = TargetConfiguration.builder()
                .mcp(mcp -> mcp.openApiSchema(ApiSchemaConfiguration.builder()
                        .s3(S3Configuration.builder().uri(configuration.openApiS3Uri()).build())
                        .build()))
                .build();
        var existingTarget = findTarget(gatewayId);

        if (existingTarget == null) {
            agentCore.createGatewayTarget(CreateGatewayTargetRequest.builder()
                    .gatewayIdentifier(gatewayId)
                    .name(configuration.targetName())
                    .description(TARGET_DESCRIPTION)
                    .targetConfiguration(targetConfiguration)
                    .build());
        } else {
            waitForTargetStable(gatewayId);
            agentCore.updateGatewayTarget(UpdateGatewayTargetRequest.builder()
                    .gatewayIdentifier(gatewayId)
                    .targetId(existingTarget.targetId())
                    .name(configuration.targetName())
                    .description(TARGET_DESCRIPTION)
                    .targetConfiguration(targetConfiguration)
                    .build());
        }
    }

    private void waitForTargetStable(String gatewayId) {
        Instant deadline = Instant.now().plus(configuration.waitTimeout());
        while (Instant.now().isBefore(deadline)) {
            var target = findTarget(gatewayId);
            if (target != null && switch (target.statusAsString()) {
                case "READY", "FAILED", "UPDATE_UNSUCCESSFUL", "SYNCHRONIZE_UNSUCCESSFUL" -> true;
                default -> false;
            }) {
                return;
            }
            pause();
        }
        throw new IllegalStateException("Timed out waiting to update gateway target "
                + configuration.targetName());
    }

    private TargetSummary findTarget(String gatewayId) {
        String nextToken = null;
        do {
            var response = agentCore.listGatewayTargets(ListGatewayTargetsRequest.builder()
                    .gatewayIdentifier(gatewayId).maxResults(1000).nextToken(nextToken).build());
            var match = response.items().stream()
                    .filter(item -> configuration.targetName().equals(item.name())).findFirst();
            if (match.isPresent()) {
                return match.get();
            }
            nextToken = response.nextToken();
        } while (nextToken != null);
        return null;
    }

    private void waitForTarget(String gatewayId) {
        Instant deadline = Instant.now().plus(configuration.waitTimeout());
        while (Instant.now().isBefore(deadline)) {
            var target = findTarget(gatewayId);
            if (target != null && "READY".equals(target.statusAsString())) {
                return;
            }
            if (target != null && ("FAILED".equals(target.statusAsString())
                    || "UPDATE_UNSUCCESSFUL".equals(target.statusAsString())
                    || "SYNCHRONIZE_UNSUCCESSFUL".equals(target.statusAsString()))) {
                throw new IllegalStateException("Gateway target did not become ready: " + target.statusAsString());
            }
            pause();
        }
        throw new IllegalStateException("Timed out waiting for gateway target " + configuration.targetName());
    }

    private static void pause() {
        try {
            Thread.sleep(5_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for AgentCore", exception);
        }
    }

    public record GatewayResult(String gatewayId, String gatewayArn, String gatewayUrl) {
    }
}
