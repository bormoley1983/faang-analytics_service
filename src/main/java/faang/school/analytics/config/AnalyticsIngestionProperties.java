package faang.school.analytics.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "app.analytics.ingestion")
public record AnalyticsIngestionProperties(
        @NotNull Duration maxFutureSkew,
        @NotNull Duration maxEventAge) {
}
