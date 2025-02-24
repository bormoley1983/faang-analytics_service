package faang.school.analytics.mapper;

import faang.school.analytics.dto.ResponseAnalyticsEventDto;
import faang.school.analytics.events.CommentEvent;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.dto.ResponseAnalyticsEventDto;
import faang.school.analytics.event.ProfileViewEvent;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.event.AnalyticsCommentEvent;
import faang.school.event.AnalyticsLikeEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnalyticsEventMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "postId", target = "receiverId")
    @Mapping(source = "authorId", target = "actorId")
    @Mapping(target = "eventType", constant = "POST_COMMENT")
    @Mapping(source = "timestamp", target = "receivedAt")
    AnalyticsEvent toAnalyticsEvent(AnalyticsCommentEvent event);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "postId", target = "receiverId")
    @Mapping(source = "authorId", target = "actorId")
    @Mapping(target = "eventType", constant = "POST_LIKE")
    @Mapping(source = "timestamp", target = "receivedAt")
    AnalyticsEvent toAnalyticsEvent(AnalyticsLikeEvent event);

    ResponseAnalyticsEventDto toAnalyticsEventDto(AnalyticsEvent analyticsEvent);
    List<ResponseAnalyticsEventDto> toAnalyticsEventDto(List<AnalyticsEvent> analyticsEvents);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "postId", target = "receiverId")
    @Mapping(source = "authorId", target = "actorId")
    @Mapping(target = "eventType", constant = "POST_COMMENT")
    @Mapping(source = "timestamp", target = "receivedAt")
    AnalyticsEvent toAnalyticsEvent(CommentEvent event);

    @Mapping(source = "userId", target = "receiverId")
    @Mapping(source = "viewerUserId", target = "actorId")
    @Mapping(target = "eventType", constant = "PROFILE_VIEW")
    @Mapping(source = "timestamp", target = "receivedAt")
    AnalyticsEvent toAnalyticsEventDto(ProfileViewEvent profileViewEvent);
}
