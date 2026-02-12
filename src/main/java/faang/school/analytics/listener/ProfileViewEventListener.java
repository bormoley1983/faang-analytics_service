package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.events.ProfileViewEvent;
import faang.school.analytics.exception.EventDeserializationException;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static faang.school.analytics.utils.ListenerErrorMessage.ERROR_PARSING_EVENT;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProfileViewEventListener implements EventListener {
    private final AnalyticsEventService analyticsEventService;
    private final AnalyticsEventMapper analyticsEventMapper;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topics.user-profile-view-topic.name}", groupId =  "${spring.kafka.consumer.group-id}")
    public void listenEvent(@Payload String eventJson) {
        try {
            ProfileViewEvent profileViewEvent = objectMapper.readValue(eventJson, ProfileViewEvent.class);
            AnalyticsEvent analyticsEvent = analyticsEventMapper.toAnalyticsEvent(profileViewEvent);
            analyticsEventService.save(analyticsEvent);
        } catch (JsonProcessingException e) {
            log.error(ERROR_PARSING_EVENT, eventJson, e);
            throw new EventDeserializationException(e.getMessage());
        }
    }
}
