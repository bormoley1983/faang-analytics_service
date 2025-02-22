package faang.school.analytics.mapper;

import faang.school.analytics.dto.ResponseAnalyticsEventDto;
import faang.school.analytics.events.CommentEvent;
import faang.school.analytics.model.AnalyticsEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnalyticsEventMapper {
    ResponseAnalyticsEventDto toAnalyticsEventDto(AnalyticsEvent analyticsEvent);
    List<ResponseAnalyticsEventDto> toAnalyticsEventDto(List<AnalyticsEvent> analyticsEvents);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "postId", target = "receiverId")
    @Mapping(source = "authorId", target = "actorId")
    @Mapping(target = "eventType", constant = "POST_COMMENT")
    @Mapping(source = "timestamp", target = "receivedAt")
    AnalyticsEvent toAnalyticsEvent(CommentEvent event);
}
