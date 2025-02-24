package faang.school.analytics.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfileViewEvent implements Event {
    private Long userId;
    private Long viewerUserId;
    private LocalDateTime timestamp;
}