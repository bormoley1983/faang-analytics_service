package faang.school.analytics.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsLikeEvent {
    private Long postId;
    private Long userId;
    private Long authorId;
    private LocalDateTime timestamp;
}
