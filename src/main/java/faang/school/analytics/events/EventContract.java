package faang.school.analytics.events;

public final class EventContract {
    public static final int CURRENT_VERSION = 1;

    private EventContract() {
    }

    public static void requireSupported(int schemaVersion) {
        if (schemaVersion != CURRENT_VERSION) {
            throw new IllegalArgumentException("Unsupported analytics event schema version: " + schemaVersion);
        }
    }
}
