package faang.school.analytics.service;

import faang.school.analytics.config.AnalyticsIngestionProperties;
import faang.school.analytics.exception.InvalidEventTimestampException;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-29T12:00:00Z");

    @Mock
    private AnalyticsEventRepository repository;

    private AnalyticsEventService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsEventService(repository,
                new AnalyticsIngestionProperties(Duration.ofMinutes(5), Duration.ofDays(365)),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void savesEventOnlyWhenInsertWinsDeduplicationRace() {
        AnalyticsEvent event = eventAt(NOW.minusSeconds(10));
        when(repository.insertIfAbsent("evt-1", 2L, 3L, "POST_LIKE",
                event.getOccurredAt(), NOW)).thenReturn(1);

        assertThat(service.save(event)).isTrue();
        assertThat(event.getReceivedAt()).isEqualTo(NOW);
        verify(repository).insertIfAbsent("evt-1", 2L, 3L, "POST_LIKE", event.getOccurredAt(), NOW);
    }

    @Test
    void reportsDuplicateAsNotInserted() {
        AnalyticsEvent event = eventAt(NOW);
        when(repository.insertIfAbsent("evt-1", 2L, 3L, "POST_LIKE", NOW, NOW)).thenReturn(0);

        assertThat(service.save(event)).isFalse();
    }

    @Test
    void rejectsEventBeyondFutureSkew() {
        assertThatThrownBy(() -> service.save(eventAt(NOW.plus(Duration.ofMinutes(6)))))
                .isInstanceOf(InvalidEventTimestampException.class);
    }

    private AnalyticsEvent eventAt(Instant occurredAt) {
        return AnalyticsEvent.builder()
                .eventId("evt-1")
                .receiverId(2L)
                .actorId(3L)
                .eventType(EventType.POST_LIKE)
                .occurredAt(occurredAt)
                .build();
    }
}
