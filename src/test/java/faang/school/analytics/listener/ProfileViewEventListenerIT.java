package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import faang.school.analytics.event.ProfileViewEvent;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@EmbeddedKafka(partitions = 1, topics = "analytics_user_view_profile_topic")
@Testcontainers
@SpringBootTest
public class ProfileViewEventListenerIT {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private ProfileViewEventListener profileViewEventListener;

    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    private final String TOPIC = "analytics_user_view_profile_topic";

    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Container
    public static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER =
            new PostgreSQLContainer<>("postgres:13.3");

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
    }

    @SneakyThrows
    @Test
    public void testEventListener() throws JsonProcessingException {
        ProfileViewEvent profileViewEvent = new ProfileViewEvent();
        profileViewEvent.setUserId(10L);
        profileViewEvent.setViewerUserId(3L);
        profileViewEvent.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(TOPIC, objectMapper.writeValueAsString(profileViewEvent))
                .thenRun(() -> {
                    await()
                            .atMost(5, TimeUnit.SECONDS)
                            .pollInterval(500, TimeUnit.MILLISECONDS)
                            .untilAsserted(() -> {
                                AnalyticsEvent analyticsEvent = analyticsEventRepository.findById(1L).orElseThrow();

                                assertNotNull(analyticsEvent);
                                assertEquals(profileViewEvent.getViewerUserId().longValue(), analyticsEvent.getActorId());
                                assertEquals(profileViewEvent.getUserId().longValue(), analyticsEvent.getReceiverId());
                                assertEquals(EventType.PROFILE_VIEW, analyticsEvent.getEventType());
                            });
                });
    }
}
