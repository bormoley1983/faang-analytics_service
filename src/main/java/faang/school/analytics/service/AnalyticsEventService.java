package faang.school.analytics.service;

import faang.school.analytics.dto.Interval;
import faang.school.analytics.dto.RequestAnalyticsEventDto;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.repository.AnalyticsEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class AnalyticsEventService {
    private final AnalyticsEventRepository analyticsEventRepository;

    @Transactional
    public void saveEvent(AnalyticsEvent event) {
        analyticsEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public List<AnalyticsEvent> getAnalytics(RequestAnalyticsEventDto requestDto) {
        if (requestDto.getInterval() != null) {
            Pair<LocalDateTime, LocalDateTime> pairInterval = Interval.of(requestDto.getInterval());
            requestDto.setFrom(pairInterval.getFirst());
            requestDto.setTo(pairInterval.getSecond());
        }

        return analyticsEventRepository.findByReceiverIdAndEventTypeBetweenDates(
                requestDto.getReceiverId(),
                requestDto.getEventType().name(),
                requestDto.getFrom(),
                requestDto.getTo());
    }
}