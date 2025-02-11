package faang.school.analytics.listener.comment;

import faang.school.analytics.event.comment.CommentEvent;
import faang.school.analytics.listener.AbstractEventListener;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CommentEventListener extends AbstractEventListener<CommentEvent> {

    private final AnalyticsEventMapper analyticsEventMapper;

    @Override
    @KafkaListener(topics = "${spring.kafka.topics.comment-topic.name}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenEvent(String eventJson) {
        handleEvent(eventJson, CommentEvent.class, analyticsEventMapper::toAnalyticsEvent);
    }
}
