package faang.school.analytics.dto;

import faang.school.analytics.model.AnalyticsEvent;
import faang.school.analytics.model.EventType;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PagedResponseTest {

    @Test
    void from_mapsPageFields() {
        // Arrange
        AnalyticsEvent event = AnalyticsEvent.builder()
                .eventId("evt-1")
                .receiverId(1L)
                .actorId(2L)
                .eventType(EventType.POST_LIKE)
                .occurredAt(Instant.parse("2026-08-29T12:00:00Z"))
                .build();
        PageImpl<AnalyticsEvent> page = new PageImpl<>(List.of(event), PageRequest.of(1, 10), 25);

        // Act
        PagedResponse<AnalyticsEvent> response = PagedResponse.from(page);

        // Assert
        assertThat(response.content()).containsExactly(event);
        assertThat(response.page()).isEqualTo(1);
        assertThat(response.size()).isEqualTo(10);
        assertThat(response.totalElements()).isEqualTo(25);
        assertThat(response.totalPages()).isEqualTo(3);
    }

    @Test
    void from_mapsEmptyPage() {
        // Arrange
        PageImpl<AnalyticsEvent> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        // Act
        PagedResponse<AnalyticsEvent> response = PagedResponse.from(page);

        // Assert
        assertThat(response.content()).isEmpty();
        assertThat(response.page()).isZero();
        assertThat(response.size()).isEqualTo(20);
        assertThat(response.totalElements()).isZero();
        assertThat(response.totalPages()).isZero();
    }

    @Test
    void constructor_copiesContentAndReturnsAnImmutableView() {
        // Arrange
        List<String> source = new ArrayList<>(List.of("first"));

        // Act
        PagedResponse<String> response = new PagedResponse<>(source, 0, 1, 1, 1);
        source.add("second");

        // Assert
        assertThat(response.content()).containsExactly("first");
        assertThatThrownBy(() -> response.content().add("third"))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
