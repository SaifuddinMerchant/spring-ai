package merchant.saifuddin.example.setup.agentcore.runtime;

import merchant.saifuddin.example.setup.config.RuntimeConfiguration;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.EntityAlreadyExistsException;

public final class RuntimeRoleManager {
    private static final String POLICY_NAME = "PetStoreAgentRuntimeAccess";

    private final IamClient iam;
    private final RuntimeConfiguration configuration;

    public RuntimeRoleManager(IamClient iam, RuntimeConfiguration configuration) {
        this.iam = iam;
        this.configuration = configuration;
    }

    public String createOrUpdate(String gatewayArn) {
        try {
            iam.createRole(request -> request
                    .roleName(configuration.roleName())
                    .description("Execution role for the pet store AgentCore Runtime")
                    .assumeRolePolicyDocument(trustPolicy()));
        } catch (EntityAlreadyExistsException ignored) {
            iam.updateAssumeRolePolicy(request -> request
                    .roleName(configuration.roleName())
                    .policyDocument(trustPolicy()));
        }

        iam.putRolePolicy(request -> request
                .roleName(configuration.roleName())
                .policyName(POLICY_NAME)
                .policyDocument(runtimePolicy(gatewayArn)));
        return iam.getRole(request -> request.roleName(configuration.roleName())).role().arn();
    }

    private String trustPolicy() {
        return """
                {
                  "Version":"2012-10-17",
                  "Statement":[{
                    "Sid":"AssumeRolePolicy",
                    "Effect":"Allow",
                    "Principal":{"Service":"bedrock-agentcore.amazonaws.com"},
                    "Action":"sts:AssumeRole",
                    "Condition":{
                      "StringEquals":{"aws:SourceAccount":"%1$s"},
                      "ArnLike":{"aws:SourceArn":"arn:aws:bedrock-agentcore:%2$s:%1$s:runtime/*"}
                    }
                  }]
                }
                """.formatted(configuration.accountId(), configuration.region().id());
    }

    private String runtimePolicy(String gatewayArn) {
        String region = configuration.region().id();
        String accountId = configuration.accountId();
        return """
                {
                  "Version":"2012-10-17",
                  "Statement":[
                    {
                      "Sid":"EcrImageAccess",
                      "Effect":"Allow",
                      "Action":["ecr:BatchGetImage","ecr:GetDownloadUrlForLayer"],
                      "Resource":"arn:aws:ecr:%1$s:%2$s:repository/%3$s"
                    },
                    {
                      "Sid":"EcrTokenAccess",
                      "Effect":"Allow",
                      "Action":"ecr:GetAuthorizationToken",
                      "Resource":"*"
                    },
                    {
                      "Sid":"RuntimeLogManagement",
                      "Effect":"Allow",
                      "Action":["logs:DescribeLogStreams","logs:CreateLogGroup","logs:PutResourcePolicy"],
                      "Resource":"arn:aws:logs:%1$s:%2$s:log-group:/aws/bedrock-agentcore/runtimes/*"
                    },
                    {
                      "Sid":"RuntimeLogWriting",
                      "Effect":"Allow",
                      "Action":["logs:CreateLogStream","logs:PutLogEvents"],
                      "Resource":"arn:aws:logs:%1$s:%2$s:log-group:/aws/bedrock-agentcore/runtimes/*:log-stream:*"
                    },
                    {
                      "Sid":"DescribeLogGroups",
                      "Effect":"Allow",
                      "Action":"logs:DescribeLogGroups",
                      "Resource":"arn:aws:logs:%1$s:%2$s:log-group:*"
                    },
                    {
                      "Sid":"RuntimeTracing",
                      "Effect":"Allow",
                      "Action":["xray:PutTraceSegments","xray:PutTelemetryRecords","xray:GetSamplingRules","xray:GetSamplingTargets"],
                      "Resource":"*"
                    },
                    {
                      "Sid":"RuntimeMetrics",
                      "Effect":"Allow",
                      "Action":"cloudwatch:PutMetricData",
                      "Resource":"*",
                      "Condition":{"StringEquals":{"cloudwatch:namespace":"bedrock-agentcore"}}
                    },
                    {
                      "Sid":"InvokeNovaInferenceProfile",
                      "Effect":"Allow",
                      "Action":["bedrock:InvokeModel","bedrock:InvokeModelWithResponseStream"],
                      "Resource":"arn:aws:bedrock:%1$s:%2$s:inference-profile/%4$s"
                    },
                    {
                      "Sid":"InvokeNovaFoundationModelsThroughProfile",
                      "Effect":"Allow",
                      "Action":["bedrock:InvokeModel","bedrock:InvokeModelWithResponseStream"],
                      "Resource":"arn:aws:bedrock:*::foundation-model/%5$s",
                      "Condition":{"StringLike":{"bedrock:InferenceProfileArn":"arn:aws:bedrock:%1$s:%2$s:inference-profile/%4$s"}}
                    },
                    {
                      "Sid":"InvokePetStoreGateway",
                      "Effect":"Allow",
                      "Action":"bedrock-agentcore:InvokeGateway",
                      "Resource":"%6$s"
                    }
                  ]
                }
                """.formatted(region, accountId, configuration.repositoryName(),
                configuration.bedrockModelId(), configuration.foundationModelId(), gatewayArn);
    }
}
