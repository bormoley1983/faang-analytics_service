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
public class ProfileViewEvent {
    private Long userId;
    private Long viewerUserId;
    private LocalDateTime timestamp;
}