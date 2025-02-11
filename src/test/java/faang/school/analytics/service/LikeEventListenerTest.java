package faang.school.analytics.service;

import faang.school.analytics.exception.LikeEventNullException;
import faang.school.analytics.mapper.AnalyticsEventMapper;
import faang.school.analytics.model.AnalyticsEvent;
import faang.school.postservice.event.LikeEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
class LikeEventListenerTest {

    @Mock
    private AnalyticsEventService analyticsEventService;

    @InjectMocks
    private LikeEventListener likeEventListener;

    @Mock
    private AnalyticsEventMapper analyticsEventMapper;

    @Test
    void likeEvent_shouldThrowException_whenEventIsNull() {
        Assertions.assertThrows(LikeEventNullException.class, () -> likeEventListener.listenLikeEvent(null));
    }

    @Test
    void likeEvent_shouldCallService_whenEventIsValid() {
        LikeEvent likeEvent = new LikeEvent(1L, 2L, 2L, LocalDateTime.now());
        AnalyticsEvent analyticsEvent = new AnalyticsEvent();

        Mockito.when(analyticsEventMapper.likeEventToAnalyticsEvent(likeEvent)).thenReturn(analyticsEvent);

        likeEventListener.listenLikeEvent(likeEvent);

        Mockito.verify(analyticsEventMapper, Mockito.times(1)).likeEventToAnalyticsEvent(likeEvent);
        Mockito.verify(analyticsEventService, Mockito.times(1)).save(analyticsEvent);
    }
}
