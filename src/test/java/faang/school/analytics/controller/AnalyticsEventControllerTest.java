package faang.school.analytics.controller;

import faang.school.analytics.dto.PagedResponse;
import faang.school.analytics.dto.RequestAnalyticsEventDto;
import faang.school.analytics.dto.ResponseAnalyticsEventDto;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.service.AnalyticsEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsEventControllerTest {

    @Mock
    private AnalyticsEventService analyticsEventService;

    @Mock
    private AnalyticsEventMapper analyticsEventMapper;

    @InjectMocks
    private AnalyticsEventController controller;

    private RequestAnalyticsEventDto requestDto;

    @BeforeEach
    void setUp() {
        requestDto = new RequestAnalyticsEventDto();
        requestDto.setReceiverId(1L);
        requestDto.setEventType(EventType.POST_LIKE);
    }

    @Test
    void getAnalytics_returnsPagedResponseWithMappedContent() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        AnalyticsEvent event = event();
        ResponseAnalyticsEventDto dto = new ResponseAnalyticsEventDto();
        dto.setEventId("evt-1");
        when(analyticsEventService.getAnalytics(eq(requestDto), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(event), pageable, 1));
        when(analyticsEventMapper.toAnalyticsEventDto(event)).thenReturn(dto);

        // Act
        ResponseEntity<PagedResponse<ResponseAnalyticsEventDto>> response =
                controller.getAnalytics(requestDto, 0, 20);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        PagedResponse<ResponseAnalyticsEventDto> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.content()).containsExactly(dto);
        assertThat(body.page()).isZero();
        assertThat(body.size()).isEqualTo(20);
        assertThat(body.totalElements()).isEqualTo(1);
        assertThat(body.totalPages()).isEqualTo(1);
        verify(analyticsEventService).getAnalytics(requestDto, pageable);
    }

    @Test
    void getAnalytics_returnsEmptyPagedResponseWhenNoEvents() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 20);
        when(analyticsEventService.getAnalytics(eq(requestDto), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        ResponseEntity<PagedResponse<ResponseAnalyticsEventDto>> response =
                controller.getAnalytics(requestDto, 0, 20);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        PagedResponse<ResponseAnalyticsEventDto> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.content()).isEmpty();
        assertThat(body.totalElements()).isZero();
    }

    @Test
    void getAnalytics_passesPageAndSizeToService() {
        // Arrange
        Pageable pageable = PageRequest.of(2, 50);
        when(analyticsEventService.getAnalytics(eq(requestDto), eq(pageable)))
                .thenReturn(Page.empty(pageable));

        // Act
        controller.getAnalytics(requestDto, 2, 50);

        // Assert
        verify(analyticsEventService).getAnalytics(requestDto, pageable);
    }

    @Test
    void searchAnalytics_returnsPagedResponseWithMappedContent() {
        // Arrange
        Pageable pageable = PageRequest.of(1, 10);
        AnalyticsEvent event = event();
        ResponseAnalyticsEventDto dto = new ResponseAnalyticsEventDto();
        dto.setEventId("evt-1");
        when(analyticsEventService.getAnalytics(eq(requestDto), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(event), pageable, 11));
        when(analyticsEventMapper.toAnalyticsEventDto(event)).thenReturn(dto);

        // Act
        ResponseEntity<PagedResponse<ResponseAnalyticsEventDto>> response =
                controller.searchAnalytics(requestDto, 1, 10);

        // Assert
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        PagedResponse<ResponseAnalyticsEventDto> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.content()).containsExactly(dto);
        assertThat(body.page()).isEqualTo(1);
        assertThat(body.size()).isEqualTo(10);
        assertThat(body.totalElements()).isEqualTo(11);
        // 11 elements / size 10 -> ceil(11/10) = 2 pages
        assertThat(body.totalPages()).isEqualTo(2);
    }

    @Test
    void searchAnalytics_propagatesServiceFailure() {
        // Arrange
        when(analyticsEventService.getAnalytics(any(), any()))
                .thenThrow(new IllegalArgumentException("from must be before or equal to to"));

        // Act / Assert
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> controller.searchAnalytics(requestDto, 0, 20))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("from must be before or equal to to");
    }

    private AnalyticsEvent event() {
        return AnalyticsEvent.builder()
                .id(1L)
                .eventId("evt-1")
                .receiverId(1L)
                .actorId(2L)
                .eventType(EventType.POST_LIKE)
                .occurredAt(Instant.parse("2026-08-29T12:00:00Z"))
                .receivedAt(Instant.parse("2026-08-29T12:00:01Z"))
                .build();
    }
}
