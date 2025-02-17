package faang.school.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import faang.school.analytics.model.EventType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseAnalyticsEventDto {
    private Long id;
    private Long receiverId;
    private Long actorId;
    private EventType eventType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime receivedAt;
}
