package merchant.saifuddin.example.setup.app;

import merchant.saifuddin.example.setup.agentcore.gateway.AgentCoreGatewayManager;
import merchant.saifuddin.example.setup.agentcore.gateway.GatewayRoleManager;
import merchant.saifuddin.example.setup.agentcore.runtime.AgentCoreRuntimeManager;
import merchant.saifuddin.example.setup.agentcore.runtime.InferenceProfileManager;
import merchant.saifuddin.example.setup.agentcore.runtime.RuntimeRoleManager;
import merchant.saifuddin.example.setup.config.GatewayConfiguration;
import merchant.saifuddin.example.setup.config.RuntimeConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.bedrock.BedrockClient;
import software.amazon.awssdk.services.iam.IamClient;

public final class PetStoreGatewaySetup {
    private PetStoreGatewaySetup() {
    }

    public static void main(String[] args) {
        var configuration = GatewayConfiguration.load();
        var runtimeConfiguration = RuntimeConfiguration.load();
        try (var iam = IamClient.builder().region(configuration.region()).build();
             var bedrock = BedrockClient.builder().region(configuration.region()).build();
             var agentCore = BedrockAgentCoreControlClient.builder()
                     .region(configuration.region()).build()) {
            var roleManager = new GatewayRoleManager(iam, configuration);
            String roleArn = roleManager.createOrUpdate();
            var gateway = new AgentCoreGatewayManager(agentCore, configuration).createOrUpdate(roleArn);
            roleManager.restrictTrustPolicy(roleArn, gateway.gatewayArn());
            System.out.printf("Gateway ready: %s (%s)%n", gateway.gatewayUrl(), gateway.gatewayId());

            var inferenceProfiles = new InferenceProfileManager(bedrock, runtimeConfiguration)
                    .createOrUpdate();
            var runtimeRoleManager = new RuntimeRoleManager(iam, runtimeConfiguration);
            String runtimeRoleArn = runtimeRoleManager.createOrUpdate(
                    gateway.gatewayArn(), inferenceProfiles.applicationProfileArn(),
                    inferenceProfiles.sourceProfileArn());
            var runtime = new AgentCoreRuntimeManager(agentCore, runtimeConfiguration)
                    .createOrUpdate(runtimeRoleArn, gateway.gatewayUrl(),
                            inferenceProfiles.applicationProfileArn());
            System.out.printf("Runtime ready: %s (version %s)%n",
                    runtime.runtimeArn(), runtime.runtimeVersion());
        }
    }
}
