package faang.school.analytics.dto;

import faang.school.analytics.model.EventType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestAnalyticsEventDto {

    @NotNull
    private Long receiverId;

    @NotNull
    private EventType eventType;

    private Interval interval;

    private LocalDateTime from = LocalDateTime.now().minusDays(3);
    private LocalDateTime to = LocalDateTime.now();
}
