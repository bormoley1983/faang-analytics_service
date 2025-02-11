package faang.school.analytics.event.comment;

import faang.school.analytics.event.Event;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentEvent implements Event {
    private long postId;
    private long authorId;
    private long commentId;
    private LocalDateTime timestamp = LocalDateTime.now();
}
