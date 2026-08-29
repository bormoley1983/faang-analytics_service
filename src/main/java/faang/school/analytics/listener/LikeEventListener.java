package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.events.AnalyticsLikeEvent;
import faang.school.analytics.events.EventContract;
import faang.school.analytics.exception.EventDeserializationException;
import faang.school.analytics.exception.LikeEventNullException;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeEventListener implements EventListener {

    private final AnalyticsEventService analyticsEventService;

    private final AnalyticsEventMapper analyticsEventMapper;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${app.kafka.topics.analytics.like}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEvent(@Payload String likeEventJson) {
        if (likeEventJson == null) {
            throw new LikeEventNullException("Like event is null");
        }

        try {
            AnalyticsLikeEvent analyticsLikeEvent = objectMapper.readValue(likeEventJson, AnalyticsLikeEvent.class);
            EventContract.requireSupported(analyticsLikeEvent.getSchemaVersion());
            AnalyticsEvent analyticsEvent = analyticsEventMapper.toAnalyticsEvent(analyticsLikeEvent);
            log.info("Received analytics event: eventId={}, type={}",
                    analyticsEvent.getEventId(), analyticsEvent.getEventType());
            analyticsEventService.save(analyticsEvent);
        } catch (JsonProcessingException e) {
            log.warn("Unable to deserialize analytics like event: payloadLength={}", likeEventJson.length());
            throw new EventDeserializationException("Unable to deserialize analytics like event", e);
        }
    }
}
