package faang.school.analytics.listener.like;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.events.AnalyticsLikeEvent;
import faang.school.analytics.exception.EventDeserializationException;
import faang.school.analytics.exception.LikeEventNullException;
import faang.school.analytics.listener.EventListener;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static faang.school.analytics.utils.listener.ListenerErrorMessage.ERROR_PARSING_EVENT;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeEventListener implements EventListener {

    private final AnalyticsEventService analyticsEventService;

    private final AnalyticsEventMapper analyticsEventMapper;

    private final ObjectMapper objectMapper;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.like-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEvent(String likeEventJson) {
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
