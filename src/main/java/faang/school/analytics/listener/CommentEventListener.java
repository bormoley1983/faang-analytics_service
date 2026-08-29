package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.events.CommentEvent;
import faang.school.analytics.events.EventContract;
import faang.school.analytics.exception.EventDeserializationException;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CommentEventListener implements EventListener {

    private final AnalyticsEventService analyticsEventService;
    private final AnalyticsEventMapper analyticsEventMapper;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topics.comment-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEvent(@Payload String jsonEvent) {
        try {
            CommentEvent commentEvent = objectMapper.readValue(jsonEvent, CommentEvent.class);
            EventContract.requireSupported(commentEvent.getSchemaVersion());
            AnalyticsEvent analyticsEvent = analyticsEventMapper.toAnalyticsEvent(commentEvent);
            log.info("Received analytics event: eventId={}, type={}",
                    analyticsEvent.getEventId(), analyticsEvent.getEventType());
            analyticsEventService.save(analyticsEvent);
        } catch (JsonProcessingException e) {
            log.warn("Unable to deserialize analytics comment event: payloadLength={}",
                    jsonEvent == null ? 0 : jsonEvent.length());
            throw new EventDeserializationException("Unable to deserialize analytics comment event", e);
        }
    }
}
