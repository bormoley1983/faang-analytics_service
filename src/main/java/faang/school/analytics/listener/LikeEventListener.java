package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.events.AnalyticsLikeEvent;
import faang.school.analytics.exception.EventDeserializationException;
import faang.school.analytics.exception.LikeEventNullException;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static faang.school.analytics.utils.ListenerErrorMessage.ERROR_PARSING_EVENT;

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

    @KafkaListener(topics = "${spring.kafka.topics.like-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEvent(@Payload String likeEventJson) {
        if (likeEventJson == null) {
            throw new LikeEventNullException("Like event is null");
        }

        try {
            AnalyticsLikeEvent analyticsLikeEvent = objectMapper.readValue(likeEventJson, AnalyticsLikeEvent.class);
            AnalyticsEvent analyticsEvent = analyticsEventMapper.toAnalyticsEvent(analyticsLikeEvent);
            log.info("Kafka listener become LikeEvent: {}", analyticsEvent);
            analyticsEventService.save(analyticsEvent);
        } catch (JsonProcessingException e) {
            log.error(ERROR_PARSING_EVENT, likeEventJson, e);
            throw new EventDeserializationException(e.getMessage());
        }
    }
}
