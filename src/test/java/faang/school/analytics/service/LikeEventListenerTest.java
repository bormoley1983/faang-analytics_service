package faang.school.analytics.service;

import faang.school.analytics.exception.LikeEventNullException;
import faang.school.postservice.event.LikeEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class KafkaListenerTest {

    @Mock
    private AnalyticsEventService analyticsEventService;

    @InjectMocks
    private LikeEventListener likeEventListener;

    @Test
    void likeEvent_shouldThrowException_whenEventIsNull() {
        Assertions.assertThrows(LikeEventNullException.class, () -> likeEventListener.likeEvent(null));
    }

    @Test
    void likeEvent_shouldCallService_whenEventIsValid() {
        LikeEvent likeEvent = new LikeEvent(1L,2L,2L);
        likeEventListener.likeEvent(likeEvent);
        Mockito.verify(analyticsEventService, Mockito.times(1)).mapToAnalyticEventAndSave(likeEvent);
    }
}
