package faang.school.analytics.dto;

import org.springframework.data.util.Pair;

import java.time.Clock;
import java.time.Instant;

public enum Interval {
    LAST_HOUR,
    LAST_THREE_HOURS,
    LAST_DAY,
    LAST_THREE_DAYS,
    LAST_WEEK,
    LAST_MONTH,
    LAST_THREE_MONTHS,
    LAST_YEAR;

    public static Pair<Instant, Instant> of(Interval interval, Clock clock) {
        Instant now = clock.instant();
        Instant from;

        switch (interval) {
            case LAST_HOUR -> from = now.minusSeconds(60 * 60);
            case LAST_THREE_HOURS -> from = now.minusSeconds(3 * 60 * 60);
            case LAST_DAY -> from = now.minusSeconds(24 * 60 * 60);
            case LAST_THREE_DAYS -> from = now.minusSeconds(3 * 24 * 60 * 60);
            case LAST_WEEK -> from = now.minusSeconds(7 * 24 * 60 * 60);
            case LAST_MONTH -> from = now.minusSeconds(30L * 24 * 60 * 60);
            case LAST_THREE_MONTHS -> from = now.minusSeconds(90L * 24 * 60 * 60);
            case LAST_YEAR -> from = now.minusSeconds(365L * 24 * 60 * 60);
            default -> throw new IllegalArgumentException("Unknown interval: " + interval);
        }

        return Pair.of(from, now);
    }
}
