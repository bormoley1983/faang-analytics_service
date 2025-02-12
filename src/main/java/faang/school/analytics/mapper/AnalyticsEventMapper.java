package faang.school.analytics.mapper;

import faang.school.analytics.dto.ResponseAnalyticsEventDto;
import faang.school.analytics.model.AnalyticsEvent;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AnalyticsEventMapper {
    ResponseAnalyticsEventDto toAnalyticsEventDto(AnalyticsEvent analyticsEvent);
    List<ResponseAnalyticsEventDto> toAnalyticsEventDto(List<AnalyticsEvent> analyticsEvents);
}
