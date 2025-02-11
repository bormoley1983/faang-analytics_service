package faang.school.analytics.service;

import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AnalyticsEventServiceTest {

    @InjectMocks
    AnalyticsEventService analyticsEventService;

    @Mock
    AnalyticsEventRepository analyticsEventRepository;

    @Test
    void saveTest() {
        AnalyticsEvent analyticsEvent = new AnalyticsEvent(1L, 2L, 2L, EventType.POST_LIKE, LocalDateTime.now());

        analyticsEventService.save(analyticsEvent);
        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(analyticsEventRepository, times(1)).save(captor.capture());
        AnalyticsEvent capturedEvent = captor.getValue();
        Assertions.assertEquals(analyticsEvent.getId(), capturedEvent.getId());
        Assertions.assertEquals(analyticsEvent.getEventType(), capturedEvent.getEventType());
        Assertions.assertEquals(analyticsEvent.getReceiverId(), capturedEvent.getReceiverId());


    }
}
