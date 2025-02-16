package faang.school.analytics.listener.like;

import faang.school.analytics.exception.LikeEventNullException;
import faang.school.analytics.listener.EventListener;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;

import faang.school.event.AnalyticsLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LikeEventListener implements EventListener<AnalyticsLikeEvent> {

    private final AnalyticsEventService analyticsEventService;

    private final AnalyticsEventMapper analyticsEventMapper;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.like-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEvent(AnalyticsLikeEvent likeEvent) {
        if (likeEvent == null) {
            throw new LikeEventNullException("Like event is null");
        }
        AnalyticsEvent analyticsEvent = analyticsEventMapper.toAnalyticsEvent(likeEvent);

        log.info("Kafka listener become LikeEvent: {}", likeEvent);
        analyticsEventService.save(analyticsEvent);
    }
}
