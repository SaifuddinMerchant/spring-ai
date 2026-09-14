package merchant.saifuddin.example.setup.config;

import software.amazon.awssdk.regions.Region;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

public record GatewayConfiguration(Region region, String gatewayName, String roleName,
                                   String targetName, String openApiS3Uri, Duration waitTimeout) {
    public static GatewayConfiguration load() {
        var properties = new Properties();
        try (InputStream input = GatewayConfiguration.class.getResourceAsStream("/application.properties")) {
            if (input == null) {
                throw new IllegalStateException("application.properties was not found");
            }
            properties.load(input);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load application.properties", exception);
        }

        return new GatewayConfiguration(
                Region.of(required(properties, "aws.region")),
                required(properties, "agentcore.gateway.name"),
                required(properties, "agentcore.gateway.role-name"),
                required(properties, "agentcore.gateway.target-name"),
                required(properties, "agentcore.gateway.openapi-s3-uri"),
                Duration.ofSeconds(Long.parseLong(required(properties,
                        "agentcore.gateway.wait-timeout-seconds"))));
    }

    private static String required(Properties properties, String key) {
        var value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required property: " + key);
        }
        return value.trim();
    }
}
