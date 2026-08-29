package faang.school.analytics.controller;


import faang.school.analytics.dto.RequestAnalyticsEventDto;
import faang.school.analytics.dto.ResponseAnalyticsEventDto;
import faang.school.analytics.dto.PagedResponse;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.service.AnalyticsEventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

@RequiredArgsConstructor
@RestController
@RequestMapping("/analytics")
@Validated
public class AnalyticsEventController {
    private final AnalyticsEventService analyticsEventService;
    private final AnalyticsEventMapper analyticsEventMapper;

    @GetMapping
    public ResponseEntity<PagedResponse<ResponseAnalyticsEventDto>> getAnalytics(
            @Valid @ModelAttribute RequestAnalyticsEventDto requestDto,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return query(requestDto, page, size);
    }

    @PostMapping
    public ResponseEntity<PagedResponse<ResponseAnalyticsEventDto>> searchAnalytics(
            @Valid @RequestBody RequestAnalyticsEventDto requestDto,
            @RequestParam(defaultValue = "0") @PositiveOrZero int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return query(requestDto, page, size);
    }

    private ResponseEntity<PagedResponse<ResponseAnalyticsEventDto>> query(
            RequestAnalyticsEventDto requestDto, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AnalyticsEvent> events = analyticsEventService.getAnalytics(requestDto, pageable);
        Page<ResponseAnalyticsEventDto> response = events.map(analyticsEventMapper::toAnalyticsEventDto);
        return ResponseEntity.ok(PagedResponse.from(response));
    }
}
