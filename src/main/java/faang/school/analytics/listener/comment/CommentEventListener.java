package faang.school.analytics.listener.comment;

import faang.school.analytics.listener.EventListener;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import faang.school.event.AnalyticsCommentEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentEventListener implements EventListener<AnalyticsCommentEvent> {

    private final AnalyticsEventService analyticsEventService;
    private final AnalyticsEventMapper analyticsEventMapper;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.comment-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEvent(AnalyticsCommentEvent event) {
        AnalyticsEvent analyticsEvent = analyticsEventMapper.toAnalyticsEvent(event);
        analyticsEventService.saveEvent(analyticsEvent);
    }
}
