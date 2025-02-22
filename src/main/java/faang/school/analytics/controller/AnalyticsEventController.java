package faang.school.analytics.controller;


import faang.school.analytics.dto.RequestAnalyticsEventDto;
import faang.school.analytics.dto.ResponseAnalyticsEventDto;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class AnalyticsEventController {
    private final AnalyticsEventService analyticsEventService;
    private final AnalyticsEventMapper analyticsEventMapper;

    @PostMapping("/analytics")
    public ResponseEntity<List<ResponseAnalyticsEventDto>> getAnalytics(@Valid @RequestBody RequestAnalyticsEventDto requestDto) {
        List<AnalyticsEvent> analyticsEvents = analyticsEventService.getAnalytics(requestDto);
        List<ResponseAnalyticsEventDto> responseDto = analyticsEventMapper.toAnalyticsEventDto(analyticsEvents);
        return ResponseEntity.ok(responseDto);
    }
}
