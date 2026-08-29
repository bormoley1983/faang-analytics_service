package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import faang.school.analytics.events.ProfileViewEvent;
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
public class ProfileViewEventListener implements EventListener {
    private final AnalyticsEventService analyticsEventService;
    private final AnalyticsEventMapper analyticsEventMapper;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${spring.kafka.topics.user-profile-view-topic.name}", groupId =  "${spring.kafka.consumer.group-id}")
    public void listenEvent(@Payload String eventJson) {
        try {
            ProfileViewEvent profileViewEvent = objectMapper.readValue(eventJson, ProfileViewEvent.class);
            EventContract.requireSupported(profileViewEvent.getSchemaVersion());
            AnalyticsEvent analyticsEvent = analyticsEventMapper.toAnalyticsEvent(profileViewEvent);
            analyticsEventService.save(analyticsEvent);
        } catch (JsonProcessingException e) {
            log.warn("Unable to deserialize profile-view event: payloadLength={}",
                    eventJson == null ? 0 : eventJson.length());
            throw new EventDeserializationException("Unable to deserialize profile-view event", e);
        }
    }
}
