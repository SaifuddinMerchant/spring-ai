package merchant.saifuddin.example.setup.agentcore.runtime;

import merchant.saifuddin.example.setup.config.RuntimeConfiguration;
import software.amazon.awssdk.services.bedrock.BedrockClient;
import software.amazon.awssdk.services.bedrock.model.CreateInferenceProfileRequest;
import software.amazon.awssdk.services.bedrock.model.InferenceProfileModelSource;
import software.amazon.awssdk.services.bedrock.model.ListInferenceProfilesRequest;
import software.amazon.awssdk.services.bedrock.model.Tag;
import software.amazon.awssdk.services.bedrock.model.TagResourceRequest;

import java.util.List;

public final class InferenceProfileManager {
    private final BedrockClient bedrock;
    private final RuntimeConfiguration configuration;

    public InferenceProfileManager(BedrockClient bedrock, RuntimeConfiguration configuration) {
        this.bedrock = bedrock;
        this.configuration = configuration;
    }

    public InferenceProfiles createOrUpdate() {
        String sourceProfileArn = bedrock.getInferenceProfile(request -> request
                .inferenceProfileIdentifier(configuration.inferenceProfileSourceId()))
                .inferenceProfileArn();
        String inferenceProfileArn = findInferenceProfileArn();
        if (inferenceProfileArn == null) {
            inferenceProfileArn = bedrock.createInferenceProfile(CreateInferenceProfileRequest.builder()
                    .inferenceProfileName(configuration.inferenceProfileName())
                    .description(configuration.inferenceProfileDescription())
                    .modelSource(InferenceProfileModelSource.builder().copyFrom(sourceProfileArn).build())
                    .tags(tag())
                    .build())
                    .inferenceProfileArn();
        }

        bedrock.tagResource(TagResourceRequest.builder()
                .resourceARN(inferenceProfileArn)
                .tags(tag())
                .build());
        return new InferenceProfiles(inferenceProfileArn, sourceProfileArn);
    }

    private String findInferenceProfileArn() {
        String nextToken = null;
        do {
            var response = bedrock.listInferenceProfiles(ListInferenceProfilesRequest.builder()
                    .maxResults(1000)
                    .nextToken(nextToken)
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
