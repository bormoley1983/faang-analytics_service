package faang.school.analytics.service;

import faang.school.analytics.exception.LikeEventNullException;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
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

    private final AnalyticsEventMapper analyticsEventMapper;

    @KafkaListener(topics = "${spring.kafka.topics.like-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenLikeEvent(LikeEvent likeEvent) {
        if (likeEvent == null) {
            throw new LikeEventNullException("Like event is null");
        }
        AnalyticsEvent analyticsEvent = analyticsEventMapper.likeEventToAnalyticsEvent(likeEvent);

        log.info("Kafka listener become LikeEvent: {}", likeEvent);
        analyticsEventService.save(analyticsEvent);
    }
}
