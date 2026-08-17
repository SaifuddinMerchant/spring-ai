package merchant.saifuddin.example.setup.app;

import merchant.saifuddin.example.setup.agentcore.gateway.AgentCoreGatewayManager;
import merchant.saifuddin.example.setup.agentcore.gateway.GatewayRoleManager;
import merchant.saifuddin.example.setup.config.GatewayConfiguration;
import software.amazon.awssdk.services.bedrockagentcorecontrol.BedrockAgentCoreControlClient;
import software.amazon.awssdk.services.iam.IamClient;

public final class PetStoreGatewaySetup {
    private PetStoreGatewaySetup() {
    }

    public static void main(String[] args) {
        var configuration = GatewayConfiguration.load();
        try (var iam = IamClient.builder().region(configuration.region()).build();
             var agentCore = BedrockAgentCoreControlClient.builder()
                     .region(configuration.region()).build()) {
            var roleManager = new GatewayRoleManager(iam, configuration);
            String roleArn = roleManager.createOrUpdate();
            var gateway = new AgentCoreGatewayManager(agentCore, configuration).createOrUpdate(roleArn);
            roleManager.restrictTrustPolicy(roleArn, gateway.gatewayArn());
            System.out.printf("Gateway ready: %s (%s)%n", gateway.gatewayUrl(), gateway.gatewayId());
        }
    }
}
