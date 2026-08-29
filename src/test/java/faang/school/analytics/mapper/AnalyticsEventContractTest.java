package faang.school.analytics.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import faang.school.analytics.events.CommentEvent;
import faang.school.analytics.model.EventType;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsEventContractTest {
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private final AnalyticsEventMapper mapper = new AnalyticsEventMapperImpl();

    @Test
    void deserializesAndMapsVersionedCommentContractWithoutMocks() throws Exception {
        String json = """
                {"eventId":"evt-comment-1","postId":42,"authorId":7,"commentId":9,
                 "timestamp":"2026-08-29T12:00:00Z"}
                """;

        CommentEvent event = objectMapper.readValue(json, CommentEvent.class);
        var analyticsEvent = mapper.toAnalyticsEvent(event);

        assertThat(analyticsEvent.getEventId()).isEqualTo("evt-comment-1");
        assertThat(analyticsEvent.getReceiverId()).isEqualTo(42L);
        assertThat(analyticsEvent.getActorId()).isEqualTo(7L);
        assertThat(analyticsEvent.getEventType()).isEqualTo(EventType.POST_COMMENT);
        assertThat(analyticsEvent.getOccurredAt()).isEqualTo(Instant.parse("2026-08-29T12:00:00Z"));
        assertThat(analyticsEvent.getReceivedAt()).isNull();
    }
}
