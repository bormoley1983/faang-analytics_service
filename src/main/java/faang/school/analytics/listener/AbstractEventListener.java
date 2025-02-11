package faang.school.analytics.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import faang.school.analytics.event.Event;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;
import java.util.function.Function;

@Slf4j
@Component
public abstract class AbstractEventListener<T extends Event> {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AnalyticsEventService analyticsEventService;

    @PostConstruct
    private void postConstruct() {
        objectMapper.registerModule(new JavaTimeModule());
    }

    protected void handleEvent(String eventJson, Class<T> eventClazz, Function<T, AnalyticsEvent> function) {
        try {
            T event = objectMapper.readValue(eventJson, eventClazz);
            log.info("Received event: {}", event);
            AnalyticsEvent analyticsEvent = function.apply(event);
            analyticsEventService.saveEvent(analyticsEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse event: {}", eventJson);
        }
    }

    public abstract void listenEvent(String eventJson);
}
