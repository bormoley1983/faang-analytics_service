package faang.school.analytics.config;

import org.testcontainers.containers.Network;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public final class IntegrationTestDependencies {

    private static final boolean CI_INTEGRATION =
            Boolean.parseBoolean(System.getenv("FAANG_CI_INTEGRATION"));
    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("apache/kafka:4.3.1");

    private static final Network TEST_NETWORK;
    private static final PostgreSQLContainer POSTGRESQL_CONTAINER;
    private static final KafkaContainer KAFKA_CONTAINER;

    static {
        if (CI_INTEGRATION) {
            TEST_NETWORK = null;
            POSTGRESQL_CONTAINER = null;
            KAFKA_CONTAINER = null;
        } else {
            TEST_NETWORK = Network.newNetwork();
            POSTGRESQL_CONTAINER = new PostgreSQLContainer(POSTGRES_IMAGE)
                    .withNetwork(TEST_NETWORK)
                    .withNetworkAliases("test-postgres");
            KAFKA_CONTAINER = new KafkaContainer(KAFKA_IMAGE)
                    .withNetwork(TEST_NETWORK)
                    .withNetworkAliases("test-kafka");
            POSTGRESQL_CONTAINER.start();
            KAFKA_CONTAINER.start();
            Runtime.getRuntime().addShutdownHook(new Thread(IntegrationTestDependencies::stopContainers));
        }
    }

    private IntegrationTestDependencies() {
    }

    public static String postgresUrl() {
        return CI_INTEGRATION
                ? requiredEnvironment("FAANG_TEST_POSTGRES_URL")
                : POSTGRESQL_CONTAINER.getJdbcUrl();
    }

    public static String postgresUsername() {
        return CI_INTEGRATION
                ? requiredEnvironment("FAANG_TEST_POSTGRES_USER")
                : POSTGRESQL_CONTAINER.getUsername();
    }

    public static String postgresPassword() {
        return CI_INTEGRATION
                ? environment("FAANG_TEST_POSTGRES_PASSWORD", "")
                : POSTGRESQL_CONTAINER.getPassword();
    }

    public static String kafkaBootstrapServers() {
        return CI_INTEGRATION
                ? requiredEnvironment("FAANG_TEST_KAFKA_BOOTSTRAP")
                : KAFKA_CONTAINER.getBootstrapServers();
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Missing required CI integration setting: " + name);
        }
        return value;
    }

    private static String environment(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }

    private static void stopContainers() {
        if (KAFKA_CONTAINER != null) {
            KAFKA_CONTAINER.stop();
        }
        if (POSTGRESQL_CONTAINER != null) {
            POSTGRESQL_CONTAINER.stop();
        }
        if (TEST_NETWORK != null) {
            TEST_NETWORK.close();
        }
    }
}
