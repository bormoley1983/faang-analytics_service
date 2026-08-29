package faang.school.analytics.dto;

import faang.school.analytics.model.EventType;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class RequestAnalyticsEventDtoTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsReversedExplicitRange() {
        RequestAnalyticsEventDto request = new RequestAnalyticsEventDto();
        request.setReceiverId(1L);
        request.setEventType(EventType.POST_LIKE);
        request.setFrom(Instant.parse("2026-08-30T00:00:00Z"));
        request.setTo(Instant.parse("2026-08-29T00:00:00Z"));

        assertThat(validator.validate(request))
                .extracting(violation -> violation.getMessage())
                .contains("from must be before or equal to to");
    }

    @Test
    void allowsNullBoundsForUtcDefaultNormalization() {
        RequestAnalyticsEventDto request = new RequestAnalyticsEventDto();
        request.setReceiverId(1L);
        request.setEventType(EventType.POST_LIKE);

        assertThat(validator.validate(request)).isEmpty();
    }
}
