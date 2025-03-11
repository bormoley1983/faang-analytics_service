package faang.school.analytics.events;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProfileViewEvent {
    private long userId;
    private long viewerUserId;
    private LocalDateTime timestamp;
}