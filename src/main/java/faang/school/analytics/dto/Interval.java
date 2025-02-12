package faang.school.analytics.dto;

import org.springframework.data.util.Pair;

import java.time.LocalDateTime;

public enum Interval {
    LAST_HOUR,
    LAST_THREE_HOURS,
    LAST_DAY,
    LAST_THREE_DAYS,
    LAST_WEEK,
    LAST_MONTH,
    LAST_THREE_MONTHS,
    LAST_YEAR;

    public static Pair<LocalDateTime, LocalDateTime> of(Interval interval) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime from;

        switch (interval) {
            case LAST_HOUR -> from = now.minusHours(1);
            case LAST_THREE_HOURS -> from = now.minusHours(3);
            case LAST_DAY -> from = now.minusDays(1);
            case LAST_THREE_DAYS -> from = now.minusDays(3);
            case LAST_WEEK -> from = now.minusDays(7);
            case LAST_MONTH -> from = now.minusMonths(1);
            case LAST_THREE_MONTHS -> from = now.minusMonths(3);
            case LAST_YEAR -> from = now.minusYears(1);
            default -> throw new IllegalArgumentException("Unknown interval: " + interval);
        }

        return Pair.of(from, now);
    }
}
