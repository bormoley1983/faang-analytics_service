package faang.school.analytics.service;

import faang.school.analytics.dto.Interval;
import faang.school.analytics.dto.RequestAnalyticsEventDto;
import faang.school.analytics.config.AnalyticsIngestionProperties;
import faang.school.analytics.exception.InvalidEventTimestampException;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnalyticsEventService {
    private final AnalyticsEventRepository analyticsEventRepository;
    private final AnalyticsIngestionProperties ingestionProperties;
    private final Clock clock;

    @Transactional
    public boolean save(AnalyticsEvent analyticsEvent) {
        Instant receivedAt = clock.instant();
        validateForIngestion(analyticsEvent, receivedAt);
        analyticsEvent.setReceivedAt(receivedAt);

        boolean inserted = analyticsEventRepository.insertIfAbsent(
                analyticsEvent.getEventId(),
                analyticsEvent.getReceiverId(),
                analyticsEvent.getActorId(),
                analyticsEvent.getEventType().name(),
                analyticsEvent.getOccurredAt(),
                receivedAt) == 1;
        log.info("Analytics event processed: eventId={}, type={}, inserted={}",
                analyticsEvent.getEventId(), analyticsEvent.getEventType(), inserted);
        return inserted;
    }


    @Transactional(readOnly = true)
    public Page<AnalyticsEvent> getAnalytics(RequestAnalyticsEventDto requestDto, Pageable pageable) {
        Instant now = clock.instant();
        Instant from = requestDto.getFrom() == null ? now.minusSeconds(3 * 24 * 60 * 60) : requestDto.getFrom();
        Instant to = requestDto.getTo() == null ? now : requestDto.getTo();
        if (requestDto.getInterval() != null) {
            Pair<Instant, Instant> pairInterval = Interval.of(requestDto.getInterval(), clock);
            from = pairInterval.getFirst();
            to = pairInterval.getSecond();
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }

        return analyticsEventRepository.findByReceiverIdAndEventTypeBetweenDates(
                requestDto.getReceiverId(),
                requestDto.getEventType().name(),
                from,
                to,
                pageable);
    }

    private void validateForIngestion(AnalyticsEvent event, Instant receivedAt) {
        if (event == null || event.getEventId() == null || event.getEventId().isBlank()
                || event.getReceiverId() == null || event.getActorId() == null
                || event.getEventType() == null || event.getOccurredAt() == null) {
            throw new IllegalArgumentException("Analytics event is missing required metadata");
        }
        if (event.getOccurredAt().isAfter(receivedAt.plus(ingestionProperties.maxFutureSkew()))) {
            throw new InvalidEventTimestampException("Event occurrence time exceeds the allowed future skew");
        }
        if (event.getOccurredAt().isBefore(receivedAt.minus(ingestionProperties.maxEventAge()))) {
            throw new InvalidEventTimestampException("Event occurrence time exceeds the allowed age");
        }
    }
}
