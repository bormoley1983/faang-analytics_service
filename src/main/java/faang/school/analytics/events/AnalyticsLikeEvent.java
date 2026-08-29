package faang.school.analytics.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsLikeEvent {
    @Builder.Default
    private int schemaVersion = EventContract.CURRENT_VERSION;
    private String eventId;
    private Long postId;
    private Long userId;
    private Long authorId;
    private Instant timestamp;
}
