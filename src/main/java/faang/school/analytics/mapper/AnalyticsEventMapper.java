package faang.school.analytics.mapper;

import faang.school.analytics.event.comment.CommentEvent;
import faang.school.analytics.model.AnalyticsEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnalyticsEventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "postId", target = "receiverId")
    @Mapping(source = "authorId", target = "actorId")
    @Mapping(target = "eventType", expression = "java(faang.school.analytics.model.EventType.of(event.getClass()))")
    @Mapping(source = "timestamp", target = "receivedAt")
    AnalyticsEvent toAnalyticsEvent(CommentEvent event);
}
