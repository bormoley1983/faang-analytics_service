package faang.school.analytics.service;

import faang.school.analytics.exception.LikeEventNullException;
import faang.school.postservice.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Slf4j
@RequiredArgsConstructor
@Service
public class LikeEventListener {

    private final AnalyticsEventService analyticsEventService;

    @KafkaListener(topics = "like-events", groupId = "analytics-group")
    public void likeEvent(LikeEvent likeEvent) {
        if (likeEvent == null) {
            throw new LikeEventNullException("Like event is null");
        }
        log.info("Kafka listener become LikeEvent: {}", likeEvent);
        analyticsEventService.mapToAnalyticEventAndSave(likeEvent);
    }
}
