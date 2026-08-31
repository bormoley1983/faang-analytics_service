package faang.school.analytics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.testcontainers.containers.Network;

import faang.school.analytics.config.kafka.KafkaTestConfig;
import faang.school.analytics.events.CommentEvent;
import faang.school.analytics.listener.CommentEventListener;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import java.lang.SuppressWarnings;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@ActiveProfiles("test")
@Import(KafkaTestConfig.class)
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
public class AnalyticsServiceIT {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsServiceIT.class);

    @Value("${spring.kafka.topics.comment-topic.name}")
    private String commentTopicName;

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
									 

    @Autowired
    private AnalyticsEventRepository analyticsEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired(required = false)
    private CommentEventListener commentEventListener;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRESQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRESQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRESQL_CONTAINER::getPassword);
        
        registry.add("spring.kafka.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.consumer.bootstrap-servers", KAFKA_CONTAINER::getBootstrapServers);
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
        registry.add("spring.main.allow-bean-definition-overriding", () -> "true");
    }

    @BeforeAll
    static void logContainerDetails() {
    try {
        log.info("=== Testcontainers Details ===");
        log.info("Postgres JDBC URL: {}", POSTGRESQL_CONTAINER.getJdbcUrl());
        log.info("Postgres Username: {}", POSTGRESQL_CONTAINER.getUsername());
        log.info("Kafka Bootstrap Servers: {}", KAFKA_CONTAINER.getBootstrapServers());
    } catch (Exception e) {
        log.error("Error initializing containers", e);
        throw new RuntimeException(e);
    }
    }

    @BeforeEach
    void setup() throws InterruptedException {
        objectMapper.registerModule(new JavaTimeModule());
        analyticsEventRepository.deleteAll();

        Thread.sleep(3000);
    
        log.info("Setup complete - Listener ready");
    }

    @Test
    void testListenerIsInitialized() {
        assertThat(commentEventListener).isNotNull();
        log.info("CommentEventListener successfully autowired: {}", commentEventListener.getClass().getName());
    }

    @Test
    void testPostgresContainerIsRunning() {
        assertThat(POSTGRESQL_CONTAINER).isNotNull();
        assertThat(POSTGRESQL_CONTAINER.isRunning()).isTrue();
        assertThat(POSTGRESQL_CONTAINER.getJdbcUrl()).isNotBlank();
        assertThat(POSTGRESQL_CONTAINER.getUsername()).isNotBlank();
        assertThat(POSTGRESQL_CONTAINER.getPassword()).isNotBlank();
    }

    @Test
    void testKafkaContainerIsRunning() {
        assertThat(KAFKA_CONTAINER).isNotNull();
        assertThat(KAFKA_CONTAINER.isRunning()).isTrue();
        assertThat(KAFKA_CONTAINER.getBootstrapServers()).isNotBlank();
    }

    @Test
    void testKafkaListenerIsRegistered() {
        log.info("=== Kafka Listener Registration Check ===");
        
        Collection<MessageListenerContainer> containers = kafkaListenerEndpointRegistry.getListenerContainers();
        log.info("Number of registered listeners: {}", containers.size());
        
        containers.forEach(container -> {
            log.info("Container ID: {}", container.getListenerId());
            log.info("Container running: {}", container.isRunning());
            String[] topics = container.getContainerProperties().getTopics();
            log.info("Container topics: {}", topics != null ? String.join(", ", topics) : "none");
        });
        
        assertThat(containers).isNotEmpty();
    }

    @Test
    void testCommentEventProcessing() throws Exception {

        CommentEvent event = new CommentEvent(1, UUID.randomUUID().toString(), 1L, 1L, 1L, Instant.now());
        String jsonEvent = objectMapper.writeValueAsString(event);

        log.info("=== Sending Message ===");
        log.info("Topic: {}", commentTopicName);
        log.info("Message: {}", jsonEvent);

        kafkaTemplate.send(commentTopicName, jsonEvent).get(10, TimeUnit.SECONDS); 
        kafkaTemplate.flush();

        log.info("Message sent successfully");

        await().pollInterval(Duration.ofMillis(500))
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                log.info("Checking database for saved events...");
                List<AnalyticsEvent> savedEvents = analyticsEventRepository.findAll();
                log.info("Found {} events in database", savedEvents.size());
                
                assertThat(savedEvents)
                    .as("Analytics events should be saved by listener")
                    .isNotEmpty()
                    .hasSize(1);

                AnalyticsEvent savedEvent = savedEvents.get(0);
                assertThat(savedEvent).isNotNull();
                assertThat(savedEvent.getReceiverId()).isEqualTo(event.getPostId());
                assertThat(savedEvent.getActorId()).isEqualTo(event.getAuthorId());
                assertThat(savedEvent.getEventType()).isEqualTo(EventType.POST_COMMENT);
                assertThat(savedEvent.getReceivedAt()).isNotNull();
            });
    }

    @Test
    void testMultipleCommentEventsProcessing() throws Exception {

        // Constructor: CommentEvent(commentId, authorId, postId, timestamp)
        CommentEvent event1 = new CommentEvent(1, UUID.randomUUID().toString(), 1L, 2L, 3L, Instant.now());
        CommentEvent event2 = new CommentEvent(1, UUID.randomUUID().toString(), 4L, 5L, 6L, Instant.now().plusSeconds(60));
        
        String jsonEvent1 = objectMapper.writeValueAsString(event1);
        String jsonEvent2 = objectMapper.writeValueAsString(event2);

        kafkaTemplate.send(commentTopicName, jsonEvent1).get(10, TimeUnit.SECONDS);
        kafkaTemplate.send(commentTopicName, jsonEvent2).get(10, TimeUnit.SECONDS);
        kafkaTemplate.flush();

        await().pollInterval(Duration.ofSeconds(1))
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<AnalyticsEvent> savedEvents = analyticsEventRepository.findAll();
                    
                    assertThat(savedEvents).hasSize(2);
                    assertThat(savedEvents)
                            .extracting(AnalyticsEvent::getEventType)
                            .containsOnly(EventType.POST_COMMENT);
                    assertThat(savedEvents)
                            .extracting(AnalyticsEvent::getActorId)
                            .containsExactlyInAnyOrder(2L, 5L);
                    assertThat(savedEvents)
                            .extracting(AnalyticsEvent::getReceiverId)
                            .containsExactlyInAnyOrder(1L, 4L);
                });
    }

    @Test
    void duplicateDeliveryPersistsOneAnalyticsFact() throws Exception {
        CommentEvent event = new CommentEvent(1, "evt-duplicate-1", 10L, 20L, 30L, Instant.now());
        String payload = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(commentTopicName, payload).get(10, TimeUnit.SECONDS);
        kafkaTemplate.send(commentTopicName, payload).get(10, TimeUnit.SECONDS);
        kafkaTemplate.flush();

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertThat(analyticsEventRepository.findAll())
                        .hasSize(1)
                        .extracting(AnalyticsEvent::getEventId)
                        .containsExactly("evt-duplicate-1"));
    }

    @Test
    void malformedMessageIsRoutedToDeadLetterTopic() throws Exception {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KAFKA_CONTAINER.getBootstrapServers());
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "analytics-dlt-test-" + UUID.randomUUID());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        try (Consumer<String, String> consumer = new DefaultKafkaConsumerFactory<>(
                props, new StringDeserializer(), new StringDeserializer()).createConsumer()) {
            consumer.subscribe(List.of(commentTopicName + ".DLT"));
            String malformedPayload = "{not-json";

            kafkaTemplate.send(commentTopicName, malformedPayload).get(10, TimeUnit.SECONDS);
            kafkaTemplate.flush();

            ConsumerRecord<String, String> deadLetter = KafkaTestUtils.getSingleRecord(
                    consumer, commentTopicName + ".DLT", Duration.ofSeconds(10));
            assertThat(deadLetter.value()).isEqualTo(malformedPayload);
            assertThat(analyticsEventRepository.findAll()).isEmpty();
        }
    }

    @Test
    void testDatabaseIsEmptyAfterCleanup() {
        List<AnalyticsEvent> events = analyticsEventRepository.findAll();
        assertThat(events).isEmpty();
    }

    @Test
    void testEventPersistenceWithValidData() throws Exception {

        Instant eventTime = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        CommentEvent event = new CommentEvent(1, UUID.randomUUID().toString(), 100L, 200L, 300L, eventTime);
        String jsonEvent = objectMapper.writeValueAsString(event);

        kafkaTemplate.send(commentTopicName, jsonEvent).get(10, TimeUnit.SECONDS);
        kafkaTemplate.flush();

        await().pollInterval(Duration.ofSeconds(1))
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    List<AnalyticsEvent> savedEvents = analyticsEventRepository.findAll();
                    
                    assertThat(savedEvents).hasSize(1);
                    AnalyticsEvent savedEvent = savedEvents.get(0);
                    assertThat(savedEvent.getId()).isNotNull();
                    assertThat(savedEvent.getEventType()).isEqualTo(EventType.POST_COMMENT);
                    assertThat(savedEvent.getActorId()).isEqualTo(200L);
                    assertThat(savedEvent.getReceiverId()).isEqualTo(100L);
                    assertThat(savedEvent.getReceivedAt()).isAfter(eventTime.minusSeconds(60));
                    assertThat(savedEvent.getReceivedAt()).isBefore(Instant.now().plusSeconds(60));
                });
    }

    @Test
    void testRepositoryConnectionIsWorking() {
        long initialCount = analyticsEventRepository.count();
        assertThat(initialCount).isEqualTo(0);
        
        assertThat(analyticsEventRepository.findAll()).isEmpty();
    }

}
