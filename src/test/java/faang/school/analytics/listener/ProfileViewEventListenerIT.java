package faang.school.analytics.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import faang.school.analytics.AnalyticsServiceIT;
import faang.school.analytics.config.kafka.KafkaTestConfig;
import faang.school.analytics.events.ProfileViewEvent;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;


@Tag("integration")
@ActiveProfiles("test")
@Import(KafkaTestConfig.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class ProfileViewEventListenerIT {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceIT.class);

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${spring.kafka.topics.user-profile-view-topic.name}")
    private String userViewProfileTopicName;

    static Network testNetwork = Network.newNetwork();

    private static final DockerImageName POSTGRES_IMAGE = DockerImageName.parse("postgres:18-alpine");
    private static final DockerImageName KAFKA_IMAGE = DockerImageName.parse("apache/kafka:4.3.1");

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer POSTGRESQL_CONTAINER =
        new PostgreSQLContainer(POSTGRES_IMAGE)
            .withNetwork(testNetwork)
            .withNetworkAliases("test-postgres");									  

    @Container
    @SuppressWarnings("resource")
    static KafkaContainer KAFKA_CONTAINER =
        new KafkaContainer(KAFKA_IMAGE)
            .withNetwork(testNetwork)
            .withNetworkAliases("test-kafka");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.datasource.hikari.schema", () -> "public");
        registry.add("spring.jpa.properties.hibernate.default_schema", () -> "public");
        registry.add("spring.liquibase.default-schema", () -> "public");
        registry.add("spring.liquibase.liquibase-schema", () -> "public");

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
    }
 
    @BeforeEach
    void setup() throws InterruptedException {
        objectMapper.registerModule(new JavaTimeModule());
        analyticsEventRepository.deleteAll();

        Thread.sleep(2000);
        log.info("Setup complete - Listener ready");
    }

    @Test
    public void testEventListener() throws Exception {

        ProfileViewEvent profileViewEvent = new ProfileViewEvent();
        profileViewEvent.setEventId(UUID.randomUUID().toString());
        profileViewEvent.setUserId(10L);
        profileViewEvent.setViewerUserId(3L);
        profileViewEvent.setTimestamp(Instant.now());

        String jsonEvent = objectMapper.writeValueAsString(profileViewEvent);
        kafkaTemplate.send(userViewProfileTopicName, jsonEvent).get(10, TimeUnit.SECONDS);
        kafkaTemplate.flush();

        await()
            .pollInterval(Duration.ofSeconds(1))
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                List<AnalyticsEvent> savedEvents = analyticsEventRepository.findAll();
                
                assertThat(savedEvents).isNotEmpty().hasSize(1);

                assertThat(savedEvents).isNotEmpty().hasSize(1);
                
                AnalyticsEvent analyticsEvent = savedEvents.get(0);
                assertThat(analyticsEvent).isNotNull();
                assertThat(analyticsEvent.getActorId()).isEqualTo(profileViewEvent.getViewerUserId());
                assertThat(analyticsEvent.getReceiverId()).isEqualTo(profileViewEvent.getUserId());
                assertThat(analyticsEvent.getEventType()).isEqualTo(EventType.PROFILE_VIEW);
                assertThat(analyticsEvent.getReceivedAt()).isNotNull();
            });
    }

}
