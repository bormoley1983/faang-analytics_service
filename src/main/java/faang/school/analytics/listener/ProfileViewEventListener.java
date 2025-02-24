package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import faang.school.analytics.event.ProfileViewEvent;
import faang.school.analytics.exception.EventDeserializationException;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ProfileViewEventListener {
    private final AnalyticsEventService analyticsEventService;
    private final AnalyticsEventMapper analyticsEventMapper;

    private final ObjectMapper objectMapper;

    public ProfileViewEventListener(AnalyticsEventService analyticsEventService, AnalyticsEventMapper analyticsEventMapper) {
        this.analyticsEventService = analyticsEventService;
        this.analyticsEventMapper = analyticsEventMapper;
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "${spring.kafka.topics.user-profile-view-topic.name}", groupId =  "${spring.kafka.consumer.group-id}")
    public void listenEvent(String eventJson) {
        try {
            ProfileViewEvent profileViewEvent = objectMapper.readValue(eventJson, ProfileViewEvent.class);
            AnalyticsEvent analyticsEvent = analyticsEventMapper.toAnalyticsEventDto(profileViewEvent);
            analyticsEventService.save(analyticsEvent);
        } catch (JsonProcessingException e) {
            log.error("Error parsing event: {}", eventJson, e);
            throw new EventDeserializationException(e.getMessage());
        }
    }
}
