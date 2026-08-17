package merchant.saifuddin.example.setup.config;

import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public record RuntimeConfiguration(Region region, String runtimeName, String roleName,
                                   String containerUri, String bedrockModelId,
                                   Duration idleSessionTimeout, Duration maxLifetime,
                                   Duration waitTimeout) {
    public static RuntimeConfiguration load() {
        var properties = new Properties();
        try (InputStream input = RuntimeConfiguration.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties was not found");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load application.properties", exception);
        }

        return new RuntimeConfiguration(
                Region.of(required(properties, "aws.region")),
                required(properties, "agentcore.runtime.name"),
                required(properties, "agentcore.runtime.role-name"),
                required(properties, "agentcore.runtime.container-uri"),
                required(properties, "agentcore.runtime.bedrock-model-id"),
                Duration.ofSeconds(Long.parseLong(required(properties,
                        "agentcore.runtime.idle-session-timeout-seconds"))),
                Duration.ofSeconds(Long.parseLong(required(properties,
                        "agentcore.runtime.max-lifetime-seconds"))),
                Duration.ofSeconds(Long.parseLong(required(properties,
                        "agentcore.runtime.wait-timeout-seconds"))));
    }

    public String accountId() {
        return containerUri.substring(0, containerUri.indexOf('.'));
    }

    public String repositoryName() {
        int repositoryStart = containerUri.indexOf('/') + 1;
        int tagStart = containerUri.lastIndexOf(':');
        return containerUri.substring(repositoryStart, tagStart);
    }

    public String foundationModelId() {
        return bedrockModelId.startsWith("us.") ? bedrockModelId.substring(3) : bedrockModelId;
    }

    private static String required(Properties properties, String key) {
        var value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value.trim();
    }
}
