package faang.school.analytics.dto;

import faang.school.analytics.model.EventType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;

import java.time.Instant;

@Data
public class RequestAnalyticsEventDto {

    @NotNull
    @Positive
    private Long receiverId;

    @NotNull
    private EventType eventType;

    private Interval interval;

    private Instant from;
    private Instant to;

    @AssertTrue(message = "from must be before or equal to to")
    public boolean isDateRangeValid() {
        return interval != null || from == null || to == null || !from.isAfter(to);
    }
}
