package faang.school.analytics.dto;

import faang.school.analytics.model.EventType;
import lombok.Data;

import java.time.Instant;

@Data
public class ResponseAnalyticsEventDto {
    private Long id;
    private String eventId;
    private Long receiverId;
    private Long actorId;
    private EventType eventType;

    private Instant occurredAt;
    private Instant receivedAt;
}
