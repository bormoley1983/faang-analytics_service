package faang.school.analytics.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import faang.school.analytics.events.CommentEvent;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentEventListenerTest {

    private static final Instant TIMESTAMP = Instant.parse("2026-08-29T12:00:00Z");

    @Mock
    private AnalyticsEventService analyticsEventService;

    @Mock
    private AnalyticsEventMapper analyticsEventMapper;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @InjectMocks
    private CommentEventListener commentEventListener;

    @Test
    void listenEvent_whenPayloadIsValid_mapsAndSaves() throws Exception {
        // Arrange
        CommentEvent commentEvent = CommentEvent.builder()
                .eventId("evt-1")
                .postId(1L)
                .authorId(2L)
                .commentId(3L)
                .timestamp(TIMESTAMP)
                .build();
        String json = objectMapper.writeValueAsString(commentEvent);
        AnalyticsEvent analyticsEvent = new AnalyticsEvent();
        when(analyticsEventMapper.toAnalyticsEvent(commentEvent)).thenReturn(analyticsEvent);

        // Act
        commentEventListener.listenEvent(json);

        // Assert
        verify(analyticsEventMapper).toAnalyticsEvent(commentEvent);
        verify(analyticsEventService).save(analyticsEvent);
    }

    @Test
    void listenEvent_whenPayloadIsMalformed_throwsDeserialization() {
        // Arrange
        String json = "{not-valid-json";

        // Act / Assert
        assertThatThrownBy(() -> commentEventListener.listenEvent(json))
                .isInstanceOf(EventDeserializationException.class)
                .hasMessageContaining("comment event");
        verifyNoInteractions(analyticsEventMapper, analyticsEventService);
    }

    @Test
    void listenEvent_whenSchemaVersionUnsupported_throwsIllegalArgument() throws Exception {
        // Arrange
        CommentEvent commentEvent = CommentEvent.builder()
                .schemaVersion(99)
                .eventId("evt-1")
                .postId(1L)
                .authorId(2L)
                .commentId(3L)
                .timestamp(TIMESTAMP)
                .build();
        String json = objectMapper.writeValueAsString(commentEvent);

        // Act / Assert
        assertThatThrownBy(() -> commentEventListener.listenEvent(json))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("schema version");
        verifyNoInteractions(analyticsEventMapper, analyticsEventService);
    }

    @Test
    void listenEvent_whenMapperReturnsNull_throwsNpeBeforeSave() throws Exception {
        // Arrange: the listener logs the mapped event before saving, so a null mapping
        // surfaces as an NPE (documented current contract).
        CommentEvent commentEvent = CommentEvent.builder()
                .eventId("evt-1")
                .postId(1L)
                .authorId(2L)
                .commentId(3L)
                .timestamp(TIMESTAMP)
                .build();
        String json = objectMapper.writeValueAsString(commentEvent);
        when(analyticsEventMapper.toAnalyticsEvent(commentEvent)).thenReturn(null);

        // Act / Assert
        assertThatThrownBy(() -> commentEventListener.listenEvent(json))
                .isInstanceOf(NullPointerException.class);
        verify(analyticsEventService, never()).save(any());
    }

    @Test
    void listenEvent_whenMapperFails_propagatesAndSkipsSave() throws Exception {
        // Arrange
        CommentEvent commentEvent = CommentEvent.builder()
                .eventId("evt-1")
                .postId(1L)
                .authorId(2L)
                .commentId(3L)
                .timestamp(TIMESTAMP)
                .build();
        String json = objectMapper.writeValueAsString(commentEvent);
        when(analyticsEventMapper.toAnalyticsEvent(any(CommentEvent.class)))
                .thenThrow(new RuntimeException("mapper failed"));

        // Act / Assert
        assertThatThrownBy(() -> commentEventListener.listenEvent(json))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("mapper failed");
        verify(analyticsEventService, never()).save(any());
    }

    @Test
    void listenEvent_whenServiceFails_propagates() throws Exception {
        // Arrange
        CommentEvent commentEvent = CommentEvent.builder()
                .eventId("evt-1")
                .postId(1L)
                .authorId(2L)
                .commentId(3L)
                .timestamp(TIMESTAMP)
                .build();
        String json = objectMapper.writeValueAsString(commentEvent);
        AnalyticsEvent analyticsEvent = new AnalyticsEvent();
        when(analyticsEventMapper.toAnalyticsEvent(commentEvent)).thenReturn(analyticsEvent);
        doThrow(new RuntimeException("db down")).when(analyticsEventService).save(any());

        // Act / Assert
        assertThatThrownBy(() -> commentEventListener.listenEvent(json))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");
    }
}
