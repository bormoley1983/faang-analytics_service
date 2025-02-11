package faang.school.analytics.service;

import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;
import faang.school.postservice.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class AnalyticsEventService {
    private final AnalyticsEventRepository analyticsEventRepository;

    public void mapToAnalyticEventAndSave(LikeEvent likeEvent) {
        AnalyticsEvent analyticsEvent = new AnalyticsEvent();
        analyticsEvent.setReceiverId(likeEvent.getPostId());
        analyticsEvent.setActorId(likeEvent.getAuthorId());
        analyticsEvent.setEventType(EventType.POST_LIKE);
        analyticsEvent.setReceivedAt(LocalDateTime.now());

        analyticsEventRepository.save(analyticsEvent);
        log.info("Like event saved with id: {}", analyticsEvent.getId());
    }
}
