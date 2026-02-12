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
public class CommentEvent implements Event {
    private Long postId;
    private Long authorId;
    private Long commentId;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
}
