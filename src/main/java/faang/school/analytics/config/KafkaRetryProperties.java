package faang.school.analytics.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.kafka.retry")
public record KafkaRetryProperties(
        @Min(1) int maxAttempts,
        @NotNull Duration backoff) {
}
