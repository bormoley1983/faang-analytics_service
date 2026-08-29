package faang.school.analytics.exception;

public class InvalidEventTimestampException extends RuntimeException {
    public InvalidEventTimestampException(String message) {
        super(message);
    }
}
