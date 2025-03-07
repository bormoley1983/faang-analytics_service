package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.config.kafka.KafkaTestConfig;
import faang.school.analytics.events.ProfileViewEvent;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(KafkaTestConfig.class)
@Testcontainers
@SpringBootTest
public class ProfileViewEventListenerIT {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ProfileViewEventListener profileViewEventListener;

    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TOPIC = "analytics_user_view_profile_topic";



    @Container
    public static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:13.3");

    @Container
    public static final KafkaContainer KAFKA_CONTAINER = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.5"));

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);

        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
    }

    @Test
    public void testEventListener() throws JsonProcessingException {
        ProfileViewEvent profileViewEvent = new ProfileViewEvent();
        profileViewEvent.setUserId(10L);
        profileViewEvent.setViewerUserId(3L);
        profileViewEvent.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(profileViewEvent));

        await()
                .pollInterval(2, TimeUnit.SECONDS)
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    AnalyticsEvent analyticsEvent = analyticsEventRepository.findById(1L).orElseThrow();

                    assertNotNull(analyticsEvent);
                    assertEquals(profileViewEvent.getViewerUserId(), analyticsEvent.getActorId());
                    assertEquals(profileViewEvent.getUserId(), analyticsEvent.getReceiverId());
                    assertEquals(EventType.PROFILE_VIEW, analyticsEvent.getEventType());
                });
    }
}
