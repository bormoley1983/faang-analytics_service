package faang.school.analytics.service;

import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import faang.school.analytics.repository.AnalyticsEventRepository;
import faang.school.postservice.event.LikeEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AnalyticsEventServiceTest {

    @InjectMocks
    AnalyticsEventService analyticsEventService;

    @Mock
    AnalyticsEventRepository analyticsEventRepository;

    @Test
    void mapToAnalyticEventAndSaveTest(){
        LikeEvent likeEvent = new LikeEvent(1L,2L,2L);

        analyticsEventService.mapToAnalyticEventAndSave(likeEvent);

        ArgumentCaptor<AnalyticsEvent> captor = ArgumentCaptor.forClass(AnalyticsEvent.class);
        verify(analyticsEventRepository, times(1)).save(captor.capture());
        AnalyticsEvent capturedEvent = captor.getValue();

        Assertions.assertEquals(likeEvent.getPostId(),capturedEvent.getReceiverId());
        Assertions.assertEquals(likeEvent.getAuthorId(),capturedEvent.getActorId());
        Assertions.assertEquals(EventType.POST_LIKE, capturedEvent.getEventType());
        Assertions.assertNotNull(capturedEvent.getReceivedAt());
    }
}
