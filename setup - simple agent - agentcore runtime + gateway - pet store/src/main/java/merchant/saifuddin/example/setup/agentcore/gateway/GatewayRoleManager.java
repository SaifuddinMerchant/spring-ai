package merchant.saifuddin.example.setup.agentcore.gateway;

import merchant.saifuddin.example.setup.config.GatewayConfiguration;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.EntityAlreadyExistsException;

public final class GatewayRoleManager {
    private static final String POLICY_NAME = "OpenApiSchemaReadAccess";

    private final IamClient iam;
    private final GatewayConfiguration configuration;

    public GatewayRoleManager(IamClient iam, GatewayConfiguration configuration) {
        this.iam = iam;
        this.configuration = configuration;
    }

    public String createOrUpdate() {
        try {
            iam.createRole(request -> request
                    .roleName(configuration.roleName())
                    .description("Execution role for the pet store AgentCore Gateway")
                    .assumeRolePolicyDocument(trustPolicy(null, null)));
        } catch (EntityAlreadyExistsException ignored) {
            iam.updateAssumeRolePolicy(request -> request
                    .roleName(configuration.roleName())
                    .policyDocument(trustPolicy(null, null)));
        }

        iam.putRolePolicy(request -> request
                .roleName(configuration.roleName())
                .policyName(POLICY_NAME)
                .policyDocument(s3ReadPolicy()));

        return iam.getRole(request -> request.roleName(configuration.roleName())).role().arn();
    }

    public void restrictTrustPolicy(String roleArn, String gatewayArn) {
        String accountId = roleArn.split(":", 6)[4];
        iam.updateAssumeRolePolicy(request -> request
                .roleName(configuration.roleName())
                .policyDocument(trustPolicy(accountId, gatewayArn)));
    }

    private static String trustPolicy(String accountId, String gatewayArn) {
        String condition = gatewayArn == null ? "" : """
                    ,"Condition":{
                      "StringEquals":{"aws:SourceAccount":"%s"},
                      "ArnEquals":{"aws:SourceArn":"%s"}
                    }
                """.formatted(accountId, gatewayArn);
        return """
                {
                  "Version":"2012-10-17",
                  "Statement":[{
                    "Sid":"GatewayAssumeRolePolicy",
                    "Effect":"Allow",
                    "Principal":{"Service":"bedrock-agentcore.amazonaws.com"},
                    "Action":"sts:AssumeRole"%s
                  }]
                }
                """.formatted(condition);
    }

    private String s3ReadPolicy() {
        String objectArn = "arn:aws:s3:::" + configuration.openApiS3Uri().substring("s3://".length());
        return """
                {
                  "Version":"2012-10-17",
                  "Statement":[{
                    "Sid":"ReadOpenApiSchema",
                    "Effect":"Allow",
                    "Action":"s3:GetObject",
                    "Resource":"%s"
                  }]
                }
                """.formatted(objectArn);
    }
}
