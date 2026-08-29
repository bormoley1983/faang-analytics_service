package faang.school.analytics.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import faang.school.analytics.events.ProfileViewEvent;
import faang.school.analytics.exception.EventDeserializationException;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileViewEventListenerTest {

    @Mock
    private AnalyticsEventService analyticsEventService;

    @Mock
    private AnalyticsEventMapper analyticsEventMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @InjectMocks
    private ProfileViewEventListener profileViewEventListener;

    @Test
    void testListenEvent_Success() {
        String eventJson = "{\"eventId\":\"evt-1\",\"userId\":123,\"viewerUserId\":456,\"timestamp\":\"2025-02-24T12:00:00Z\"}";

        ProfileViewEvent profileViewEvent = new ProfileViewEvent();
        profileViewEvent.setEventId("evt-1");
        profileViewEvent.setUserId(123L);
        profileViewEvent.setViewerUserId(456L);
        profileViewEvent.setTimestamp(Instant.parse("2025-02-24T12:00:00Z"));

        AnalyticsEvent analyticsEvent = new AnalyticsEvent();

        when(analyticsEventMapper.toAnalyticsEvent(profileViewEvent)).thenReturn(analyticsEvent);

        profileViewEventListener.listenEvent(eventJson);

        verify(analyticsEventMapper).toAnalyticsEvent(profileViewEvent);
        verify(analyticsEventService).save(analyticsEvent);
    }

    @Test
    void testListenEvent_DeserializationError() {
        String eventJson = "{\"userId\": 123, \"viewerUserId\": 456, \"timestamp\": \"invalid-time\"}";

        assertThrows(EventDeserializationException.class, () -> profileViewEventListener.listenEvent(eventJson));

        verifyNoInteractions(analyticsEventService, analyticsEventMapper);
    }

    
    @Test
    void testListenEvent_NullMappedEvent() {
        String eventJson = "{\"eventId\":\"evt-1\",\"userId\":123,\"viewerUserId\":456,\"timestamp\":\"2025-02-24T12:00:00Z\"}";

        ProfileViewEvent profileViewEvent = new ProfileViewEvent();
        profileViewEvent.setEventId("evt-1");
        profileViewEvent.setUserId(123L);
        profileViewEvent.setViewerUserId(456L);
        profileViewEvent.setTimestamp(Instant.parse("2025-02-24T12:00:00Z"));

        when(analyticsEventMapper.toAnalyticsEvent(profileViewEvent)).thenReturn(null);

        profileViewEventListener.listenEvent(eventJson);

        verify(analyticsEventMapper).toAnalyticsEvent(profileViewEvent);
        verify(analyticsEventService).save(null);
    }

    @Test
    void testListenEvent_MapperThrowsRuntimeException() {
        String eventJson = "{\"eventId\":\"evt-1\",\"userId\":123,\"viewerUserId\":456,\"timestamp\":\"2025-02-24T12:00:00Z\"}";

        ProfileViewEvent profileViewEvent = new ProfileViewEvent();
        profileViewEvent.setEventId("evt-1");
        profileViewEvent.setUserId(123L);
        profileViewEvent.setViewerUserId(456L);
        profileViewEvent.setTimestamp(Instant.parse("2025-02-24T12:00:00Z"));

        when(analyticsEventMapper.toAnalyticsEvent(profileViewEvent)).thenThrow(new RuntimeException("mapper failed"));

        assertThrows(RuntimeException.class, () -> profileViewEventListener.listenEvent(eventJson));

        verifyNoInteractions(analyticsEventService);
    }
}

