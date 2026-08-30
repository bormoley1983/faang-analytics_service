package faang.school.analytics.service;

import faang.school.analytics.config.AnalyticsIngestionProperties;
import faang.school.analytics.dto.Interval;
import faang.school.analytics.dto.RequestAnalyticsEventDto;
import faang.school.analytics.exception.InvalidEventTimestampException;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventServiceTest {
    private static final Instant NOW = Instant.parse("2026-08-29T12:00:00Z");
    private static final Duration MAX_FUTURE_SKEW = Duration.ofMinutes(5);
    private static final Duration MAX_EVENT_AGE = Duration.ofDays(365);

    @Mock
    private AnalyticsEventRepository repository;

    private AnalyticsEventService service;

    @BeforeEach
    void setUp() {
        service = new AnalyticsEventService(repository,
                new AnalyticsIngestionProperties(MAX_FUTURE_SKEW, MAX_EVENT_AGE),
                Clock.fixed(NOW, ZoneOffset.UTC));
    }

    // ------------------------------------------------------------------ save

    @Test
    void savesEventOnlyWhenInsertWinsDeduplicationRace() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minusSeconds(10));
        when(repository.insertIfAbsent("evt-1", 2L, 3L, "POST_LIKE",
                event.getOccurredAt(), NOW)).thenReturn(1);

        // Act
        boolean inserted = service.save(event);

        // Assert
        assertThat(inserted).isTrue();
        assertThat(event.getReceivedAt()).isEqualTo(NOW);
        verify(repository).insertIfAbsent("evt-1", 2L, 3L, "POST_LIKE", event.getOccurredAt(), NOW);
    }

    @Test
    void reportsDuplicateAsNotInserted() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW);
        when(repository.insertIfAbsent("evt-1", 2L, 3L, "POST_LIKE", NOW, NOW)).thenReturn(0);

        // Act
        boolean inserted = service.save(event);

        // Assert
        assertThat(inserted).isFalse();
    }

    @Test
    void rejectsEventBeyondFutureSkew() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.plus(MAX_FUTURE_SKEW).plusSeconds(1));

        // Act / Assert
        assertThatThrownBy(() -> service.save(event))
                .isInstanceOf(InvalidEventTimestampException.class)
                .hasMessageContaining("future skew");
        verifyNoInteractions(repository);
    }

    @Test
    void acceptsEventExactlyAtFutureSkewBoundary() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.plus(MAX_FUTURE_SKEW));
        when(repository.insertIfAbsent(any(), anyLong(), anyLong(), any(), any(), any())).thenReturn(1);

        // Act
        boolean inserted = service.save(event);

        // Assert
        assertThat(inserted).isTrue();
    }

    @Test
    void rejectsEventOlderThanMaxAge() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minus(MAX_EVENT_AGE).minusSeconds(1));

        // Act / Assert
        assertThatThrownBy(() -> service.save(event))
                .isInstanceOf(InvalidEventTimestampException.class)
                .hasMessageContaining("age");
        verifyNoInteractions(repository);
    }

    @Test
    void acceptsEventExactlyAtMaxAgeBoundary() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minus(MAX_EVENT_AGE));
        when(repository.insertIfAbsent(any(), anyLong(), anyLong(), any(), any(), any())).thenReturn(1);

        // Act
        boolean inserted = service.save(event);

        // Assert
        assertThat(inserted).isTrue();
    }

    @Test
    void rejectsNullEvent() {
        // Act / Assert
        assertThatThrownBy(() -> service.save(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing required metadata");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsEventWithBlankEventId() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minusSeconds(1));
        event.setEventId("  ");

        // Act / Assert
        assertThatThrownBy(() -> service.save(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing required metadata");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsEventWithMissingReceiverId() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minusSeconds(1));
        event.setReceiverId(null);

        // Act / Assert
        assertThatThrownBy(() -> service.save(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing required metadata");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsEventWithMissingActorId() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minusSeconds(1));
        event.setActorId(null);

        // Act / Assert
        assertThatThrownBy(() -> service.save(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing required metadata");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsEventWithMissingEventType() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minusSeconds(1));
        event.setEventType(null);

        // Act / Assert
        assertThatThrownBy(() -> service.save(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing required metadata");
        verifyNoInteractions(repository);
    }

    @Test
    void rejectsEventWithMissingOccurredAt() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minusSeconds(1));
        event.setOccurredAt(null);

        // Act / Assert
        assertThatThrownBy(() -> service.save(event))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("missing required metadata");
        verifyNoInteractions(repository);
    }

    @Test
    void propagatesRepositoryFailureOnSave() {
        // Arrange
        AnalyticsEvent event = eventAt(NOW.minusSeconds(1));
        when(repository.insertIfAbsent(any(), anyLong(), anyLong(), any(), any(), any()))
                .thenThrow(new RuntimeException("db down"));

        // Act / Assert
        assertThatThrownBy(() -> service.save(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");
    }

    // ----------------------------------------------------------- getAnalytics

    @Test
    void queriesWithDefaultThreeDayWindowWhenNoFilters() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("POST_LIKE"), any(), any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        Page<AnalyticsEvent> result = service.getAnalytics(request, pageable);

        // Assert
        assertThat(result).isEmpty();
        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("POST_LIKE"),
                fromCaptor.capture(), toCaptor.capture(), eq(pageable));
        assertThat(fromCaptor.getValue()).isEqualTo(NOW.minus(Duration.ofDays(3)));
        assertThat(toCaptor.getValue()).isEqualTo(NOW);
    }

    @Test
    void queriesWithExplicitFromAndTo() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        Instant from = NOW.minus(Duration.ofHours(2));
        Instant to = NOW.minus(Duration.ofMinutes(30));
        request.setFrom(from);
        request.setTo(to);
        Pageable pageable = PageRequest.of(1, 5);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("POST_LIKE"), eq(from), eq(to), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        service.getAnalytics(request, pageable);

        // Assert
        verify(repository).findByReceiverIdAndEventTypeBetweenDates(1L, "POST_LIKE", from, to, pageable);
    }

    @Test
    void queriesWithOnlyFromDefaultsToNow() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        Instant from = NOW.minus(Duration.ofHours(1));
        request.setFrom(from);
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("POST_LIKE"), eq(from), eq(NOW), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        service.getAnalytics(request, pageable);

        // Assert
        verify(repository).findByReceiverIdAndEventTypeBetweenDates(1L, "POST_LIKE", from, NOW, pageable);
    }

    @Test
    void queriesWithOnlyToDefaultsFromToThreeDaysAgo() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        Instant to = NOW.minus(Duration.ofHours(1));
        request.setTo(to);
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("POST_LIKE"), any(), eq(to), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        service.getAnalytics(request, pageable);

        // Assert
        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("POST_LIKE"),
                fromCaptor.capture(), eq(to), eq(pageable));
        assertThat(fromCaptor.getValue()).isEqualTo(NOW.minus(Duration.ofDays(3)));
    }

    @Test
    void queriesWithIntervalOverrideExplicitBounds() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.PROFILE_VIEW);
        request.setInterval(Interval.LAST_DAY);
        request.setFrom(NOW.minus(Duration.ofHours(1)));
        request.setTo(NOW.minus(Duration.ofMinutes(1)));
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("PROFILE_VIEW"), any(), any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        service.getAnalytics(request, pageable);

        // Assert: interval wins over explicit from/to
        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("PROFILE_VIEW"),
                fromCaptor.capture(), toCaptor.capture(), eq(pageable));
        assertThat(fromCaptor.getValue()).isEqualTo(NOW.minus(Duration.ofDays(1)));
        assertThat(toCaptor.getValue()).isEqualTo(NOW);
    }

    @Test
    void queriesWithEachIntervalValue() {
        // Act / Assert: each interval maps to its documented window (fresh stub per call)
        assertIntervalWindow(Interval.LAST_HOUR, Duration.ofHours(1));
        assertIntervalWindow(Interval.LAST_THREE_HOURS, Duration.ofHours(3));
        assertIntervalWindow(Interval.LAST_DAY, Duration.ofDays(1));
        assertIntervalWindow(Interval.LAST_THREE_DAYS, Duration.ofDays(3));
        assertIntervalWindow(Interval.LAST_WEEK, Duration.ofDays(7));
        assertIntervalWindow(Interval.LAST_MONTH, Duration.ofDays(30));
        assertIntervalWindow(Interval.LAST_THREE_MONTHS, Duration.ofDays(90));
        assertIntervalWindow(Interval.LAST_YEAR, Duration.ofDays(365));
    }

    @Test
    void rejectsReversedExplicitRange() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        request.setFrom(NOW);
        request.setTo(NOW.minus(Duration.ofHours(1)));
        Pageable pageable = PageRequest.of(0, 20);

        // Act / Assert
        assertThatThrownBy(() -> service.getAnalytics(request, pageable))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("from must be before or equal to to");
        verifyNoInteractions(repository);
    }

    @Test
    void acceptsEqualFromAndToBoundary() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        Instant instant = NOW.minus(Duration.ofHours(1));
        request.setFrom(instant);
        request.setTo(instant);
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("POST_LIKE"), eq(instant), eq(instant), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        service.getAnalytics(request, pageable);

        // Assert
        verify(repository).findByReceiverIdAndEventTypeBetweenDates(1L, "POST_LIKE", instant, instant, pageable);
    }

    @Test
    void returnsEmptyPageWhenNoEventsMatch() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(anyLong(), any(), any(), any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        Page<AnalyticsEvent> result = service.getAnalytics(request, pageable);

        // Assert
        assertThat(result).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void returnsPagedEventsWithContent() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        Pageable pageable = PageRequest.of(0, 20);
        AnalyticsEvent stored = eventAt(NOW.minusSeconds(60));
        when(repository.findByReceiverIdAndEventTypeBetweenDates(anyLong(), any(), any(), any(), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(stored), pageable, 1));

        // Act
        Page<AnalyticsEvent> result = service.getAnalytics(request, pageable);

        // Assert
        assertThat(result.getContent()).containsExactly(stored);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void propagatesRepositoryFailureOnQuery() {
        // Arrange
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(anyLong(), any(), any(), any(), eq(pageable)))
                .thenThrow(new RuntimeException("db down"));

        // Act / Assert
        assertThatThrownBy(() -> service.getAnalytics(request, pageable))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("db down");
    }

    // --------------------------------------------------------------- helpers

    private void assertIntervalWindow(Interval interval, Duration expectedWindow) {
        // reset accumulated invocations from previous interval assertions in the same test
        clearInvocations(repository);
        Pageable pageable = PageRequest.of(0, 20);
        RequestAnalyticsEventDto request = request(EventType.POST_LIKE);
        request.setInterval(interval);
        when(repository.findByReceiverIdAndEventTypeBetweenDates(anyLong(), any(), any(), any(), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        service.getAnalytics(request, pageable);

        ArgumentCaptor<Instant> fromCaptor = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCaptor = ArgumentCaptor.forClass(Instant.class);
        verify(repository).findByReceiverIdAndEventTypeBetweenDates(eq(1L), eq("POST_LIKE"),
                fromCaptor.capture(), toCaptor.capture(), eq(pageable));
        assertThat(fromCaptor.getValue()).isEqualTo(NOW.minus(expectedWindow));
        assertThat(toCaptor.getValue()).isEqualTo(NOW);
        // exactly one repository call per interval
        verifyNoMoreInteractions(repository);
    }

    private RequestAnalyticsEventDto request(EventType eventType) {
        RequestAnalyticsEventDto request = new RequestAnalyticsEventDto();
        request.setReceiverId(1L);
        request.setEventType(eventType);
        return request;
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
