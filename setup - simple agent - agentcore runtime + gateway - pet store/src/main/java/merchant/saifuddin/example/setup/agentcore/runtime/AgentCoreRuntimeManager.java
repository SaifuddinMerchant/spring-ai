package merchant.saifuddin.example.setup.agentcore.runtime;

import merchant.saifuddin.example.setup.config.RuntimeConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.bedrockagentcorecontrol.model.*;

import java.time.Instant;
import java.util.Map;

public final class AgentCoreRuntimeManager {
    private static final String DESCRIPTION = "Pet store agent using Bedrock and MCP gateway tools";

    private final BedrockAgentCoreControlClient agentCore;
    private final RuntimeConfiguration configuration;

    public AgentCoreRuntimeManager(BedrockAgentCoreControlClient agentCore,
                                   RuntimeConfiguration configuration) {
        this.agentCore = agentCore;
        this.configuration = configuration;
    }

    public RuntimeResult createOrUpdate(String roleArn, String gatewayUrl) {
        var existing = findRuntime();
        String runtimeId;
        if (existing == null) {
            runtimeId = agentCore.createAgentRuntime(CreateAgentRuntimeRequest.builder()
                    .agentRuntimeName(configuration.runtimeName())
                    .description(DESCRIPTION)
                    .agentRuntimeArtifact(artifact())
                    .roleArn(roleArn)
                    .networkConfiguration(network -> network.networkMode("PUBLIC"))
                    .protocolConfiguration(protocol -> protocol.serverProtocol("HTTP"))
                    .lifecycleConfiguration(this::configureLifecycle)
                    .environmentVariables(environment(gatewayUrl))
                    .build()).agentRuntimeId();
            waitForRuntime(runtimeId);
        } else {
            runtimeId = existing.agentRuntimeId();
            waitForRuntimeStable(runtimeId);
        }

        agentCore.updateAgentRuntime(UpdateAgentRuntimeRequest.builder()
                .agentRuntimeId(runtimeId)
                .description(DESCRIPTION)
                .agentRuntimeArtifact(artifact())
                .roleArn(roleArn)
                .networkConfiguration(network -> network.networkMode("PUBLIC"))
                .protocolConfiguration(protocol -> protocol.serverProtocol("HTTP"))
                .lifecycleConfiguration(this::configureLifecycle)
                .metadataConfiguration(metadata -> metadata.requireMMDSV2(true))
                .environmentVariables(environment(gatewayUrl))
                .build());

        var runtime = waitForRuntime(runtimeId);
        return new RuntimeResult(runtime.agentRuntimeId(), runtime.agentRuntimeArn(),
                runtime.agentRuntimeVersion());
    }

    private AgentRuntimeArtifact artifact() {
        return AgentRuntimeArtifact.builder()
                .containerConfiguration(container -> container.containerUri(configuration.containerUri()))
                .build();
    }

    private Map<String, String> environment(String gatewayUrl) {
        return Map.of(
                "BEDROCK_MODEL_ID", configuration.bedrockModelId(),
                "PET_STORE_GATEWAY_URL", gatewayUrl);
    }

    private void configureLifecycle(LifecycleConfiguration.Builder lifecycle) {
        lifecycle
                .idleRuntimeSessionTimeout(Math.toIntExact(configuration.idleSessionTimeout().toSeconds()))
                .maxLifetime(Math.toIntExact(configuration.maxLifetime().toSeconds()));
    }

    private AgentRuntime findRuntime() {
        String nextToken = null;
        do {
            var response = agentCore.listAgentRuntimes(ListAgentRuntimesRequest.builder()
                    .maxResults(100).nextToken(nextToken).build());
            var match = response.agentRuntimes().stream()
                    .filter(runtime -> configuration.runtimeName().equals(runtime.agentRuntimeName()))
                    .findFirst();
            if (match.isPresent()) {
                return match.get();
            }
            nextToken = response.nextToken();
        } while (nextToken != null);
        return null;
    }

    private GetAgentRuntimeResponse waitForRuntime(String runtimeId) {
        Instant deadline = Instant.now().plus(configuration.waitTimeout());
        while (Instant.now().isBefore(deadline)) {
            var runtime = getRuntime(runtimeId);
            if ("READY".equals(runtime.statusAsString())) {
                return runtime;
            }
            if ("CREATE_FAILED".equals(runtime.statusAsString())
                    || "UPDATE_FAILED".equals(runtime.statusAsString())) {
                throw new IllegalStateException("Runtime did not become ready: " + runtime.failureReason());
            }
            pause();
        }
        throw new IllegalStateException("Timed out waiting for runtime " + runtimeId);
    }

    private void waitForRuntimeStable(String runtimeId) {
        Instant deadline = Instant.now().plus(configuration.waitTimeout());
        while (Instant.now().isBefore(deadline)) {
            String status = getRuntime(runtimeId).statusAsString();
            if ("READY".equals(status) || "CREATE_FAILED".equals(status)
                    || "UPDATE_FAILED".equals(status)) {
                return;
            }
            pause();
        }
        throw new IllegalStateException("Timed out waiting to update runtime " + runtimeId);
    }

    private GetAgentRuntimeResponse getRuntime(String runtimeId) {
        return agentCore.getAgentRuntime(GetAgentRuntimeRequest.builder()
                .agentRuntimeId(runtimeId).build());
    }

    private static void pause() {
        try {
            Thread.sleep(10_000);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for AgentCore Runtime", exception);
        }
    }

    public record RuntimeResult(String runtimeId, String runtimeArn, String runtimeVersion) {
    }
}
