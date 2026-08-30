package faang.school.analytics.dto;

import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class IntervalTest {

    private static final Instant NOW = Instant.parse("2026-08-29T12:00:00Z");
    private final Clock clock = Clock.fixed(NOW, ZoneOffset.UTC);

    @Test
    void lastHour_returnsOneHourWindow() {
        // Act
        Pair<Instant, Instant> window = Interval.of(Interval.LAST_HOUR, clock);

        // Assert
        assertThat(window.getFirst()).isEqualTo(NOW.minus(Duration.ofHours(1)));
        assertThat(window.getSecond()).isEqualTo(NOW);
    }

    @Test
    void lastThreeHours_returnsThreeHourWindow() {
        // Act
        Pair<Instant, Instant> window = Interval.of(Interval.LAST_THREE_HOURS, clock);

        // Assert
        assertThat(window.getFirst()).isEqualTo(NOW.minus(Duration.ofHours(3)));
        assertThat(window.getSecond()).isEqualTo(NOW);
    }

    @Test
    void lastDay_returnsOneDayWindow() {
        // Act
        Pair<Instant, Instant> window = Interval.of(Interval.LAST_DAY, clock);

        // Assert
        assertThat(window.getFirst()).isEqualTo(NOW.minus(Duration.ofDays(1)));
        assertThat(window.getSecond()).isEqualTo(NOW);
    }

    @Test
    void lastThreeDays_returnsThreeDayWindow() {
        // Act
        Pair<Instant, Instant> window = Interval.of(Interval.LAST_THREE_DAYS, clock);

        // Assert
        assertThat(window.getFirst()).isEqualTo(NOW.minus(Duration.ofDays(3)));
        assertThat(window.getSecond()).isEqualTo(NOW);
    }

    @Test
    void lastWeek_returnsSevenDayWindow() {
        // Act
        Pair<Instant, Instant> window = Interval.of(Interval.LAST_WEEK, clock);

        // Assert
        assertThat(window.getFirst()).isEqualTo(NOW.minus(Duration.ofDays(7)));
        assertThat(window.getSecond()).isEqualTo(NOW);
    }

    @Test
    void lastMonth_returnsThirtyDayWindow() {
        // Act
        Pair<Instant, Instant> window = Interval.of(Interval.LAST_MONTH, clock);

        // Assert
        assertThat(window.getFirst()).isEqualTo(NOW.minus(Duration.ofDays(30)));
        assertThat(window.getSecond()).isEqualTo(NOW);
    }

    @Test
    void lastThreeMonths_returnsNinetyDayWindow() {
        // Act
        Pair<Instant, Instant> window = Interval.of(Interval.LAST_THREE_MONTHS, clock);

        // Assert
        assertThat(window.getFirst()).isEqualTo(NOW.minus(Duration.ofDays(90)));
        assertThat(window.getSecond()).isEqualTo(NOW);
    }

    @Test
    void lastYear_returnsThreeHundredSixtyFiveDayWindow() {
        // Act
        Pair<Instant, Instant> window = Interval.of(Interval.LAST_YEAR, clock);

        // Assert
        assertThat(window.getFirst()).isEqualTo(NOW.minus(Duration.ofDays(365)));
        assertThat(window.getSecond()).isEqualTo(NOW);
    }
}
