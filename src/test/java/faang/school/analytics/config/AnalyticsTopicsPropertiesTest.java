package faang.school.analytics.config;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AnalyticsTopicsPropertiesTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void acceptsCanonicalTopic() {
        assertThat(validator.validate(new AnalyticsTopicsProperties("analytics_like_topic"))).isEmpty();
    }

    @Test
    void rejectsBlankOrInvalidTopicAtStartupBindingBoundary() {
        assertThat(validator.validate(new AnalyticsTopicsProperties("analytics like topic"))).isNotEmpty();
        assertThat(validator.validate(new AnalyticsTopicsProperties(""))).isNotEmpty();
    }
}
