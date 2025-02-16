package faang.school.analytics.mapper;

import faang.school.analytics.model.AnalyticsEvent;
import faang.school.postservice.event.LikeEvent;
import faang.school.event.AnalyticsCommentEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnalyticsEventMapper {

    @Mapping(target = "eventType", expression = "java(faang.school.analytics.model.EventType.POST_COMMENT)")
    AnalyticsEvent toAnalyticsEvent(AnalyticsCommentEvent event);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "postId", target = "receiverId")
    @Mapping(source = "authorId", target = "actorId")
    @Mapping(target = "eventType", constant = "POST_LIKE")
    @Mapping(source = "timestamp", target = "receivedAt")
    AnalyticsEvent likeEventToAnalyticsEvent(LikeEvent event);
}
