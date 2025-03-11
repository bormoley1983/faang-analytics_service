package faang.school.analytics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import faang.school.analytics.config.kafka.KafkaTestConfig;
import faang.school.analytics.events.CommentEvent;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Import(KafkaTestConfig.class)
@Testcontainers
@SpringBootTest
public class AnalyticsServiceIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withUsername("user")
            .withPassword("password");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.3.5")
    );

    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @BeforeEach
    void setup() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testCommentEventProcessing() throws JsonProcessingException {
        CommentEvent event = new CommentEvent(1L, 1L, 1L, LocalDateTime.now());
        String jsonEvent = objectMapper.writeValueAsString(event);

        kafkaTemplate.send("analytics_comment_topic", jsonEvent);

        await().pollInterval(Duration.ofSeconds(3))
                .atMost(10, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    AnalyticsEvent savedEvent = analyticsEventRepository.findAll().get(0);

                    assertNotNull(savedEvent);
                    assertEquals(event.getPostId(), savedEvent.getReceiverId());
                    assertEquals(event.getAuthorId(), savedEvent.getActorId());
                    assertEquals(EventType.POST_COMMENT, savedEvent.getEventType());
                });
    }
}
