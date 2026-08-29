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
public class ProfileViewEvent {
    @Builder.Default
    private int schemaVersion = EventContract.CURRENT_VERSION;
    private String eventId;
    private Long userId;
    private Long viewerUserId;
    private Instant timestamp;
}
