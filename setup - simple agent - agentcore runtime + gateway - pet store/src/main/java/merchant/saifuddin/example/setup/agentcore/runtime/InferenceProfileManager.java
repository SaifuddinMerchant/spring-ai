package merchant.saifuddin.example.setup.agentcore.runtime;

import merchant.saifuddin.example.setup.config.RuntimeConfiguration;
import software.amazon.awssdk.services.bedrock.BedrockClient;
import software.amazon.awssdk.services.bedrock.model.CreateInferenceProfileRequest;
import software.amazon.awssdk.services.bedrock.model.InferenceProfileModelSource;
import software.amazon.awssdk.services.bedrock.model.InferenceProfileType;
import software.amazon.awssdk.services.bedrock.model.ListInferenceProfilesRequest;
import software.amazon.awssdk.services.bedrock.model.Tag;

import java.util.List;

public final class InferenceProfileManager {
    private final BedrockClient bedrock;
    private final RuntimeConfiguration configuration;

    public InferenceProfileManager(BedrockClient bedrock, RuntimeConfiguration configuration) {
        this.bedrock = bedrock;
        this.configuration = configuration;
    }

    public InferenceProfiles findOrCreate() {
        String inferenceProfileArn = findApplicationInferenceProfileArn();
        String sourceProfileArn = bedrock.getInferenceProfile(request -> request
                .inferenceProfileIdentifier(configuration.inferenceProfileSourceId()))
                .inferenceProfileArn();
        if (inferenceProfileArn == null) {
            inferenceProfileArn = bedrock.createInferenceProfile(CreateInferenceProfileRequest.builder()
                    .inferenceProfileName(configuration.inferenceProfileName())
                    .description(configuration.inferenceProfileDescription())
                    .modelSource(InferenceProfileModelSource.builder().copyFrom(sourceProfileArn).build())
                    .tags(tag())
                    .build())
                    .inferenceProfileArn();
        }

        return new InferenceProfiles(inferenceProfileArn, sourceProfileArn);
    }

    private String findApplicationInferenceProfileArn() {
        String nextToken = null;
        do {
            var response = bedrock.listInferenceProfiles(ListInferenceProfilesRequest.builder()
                    .maxResults(1000)
                    .nextToken(nextToken)
                    .typeEquals(InferenceProfileType.APPLICATION)
                    .build());
            var match = response.inferenceProfileSummaries().stream()
                    .filter(profile -> configuration.inferenceProfileName()
                            .equals(profile.inferenceProfileName()))
                    .findFirst();
            if (match.isPresent()) {
                return match.get().inferenceProfileArn();
            }
            nextToken = response.nextToken();
        } while (nextToken != null);
        return null;
    }

    private List<Tag> tag() {
        return List.of(Tag.builder()
                .key(configuration.inferenceProfileTagKey())
                .value(configuration.inferenceProfileTagValue())
                .build());
    }

    public record InferenceProfiles(String applicationProfileArn, String sourceProfileArn) {
    }
}
