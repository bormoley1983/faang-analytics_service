package faang.school.analytics.events;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EventContractTest {

    @Test
    void requireSupported_acceptsCurrentVersion() {
        // Act / Assert
        assertThatCode(() -> EventContract.requireSupported(EventContract.CURRENT_VERSION))
                .doesNotThrowAnyException();
    }

    @Test
    void requireSupported_rejectsOlderVersion() {
        // Act / Assert
        assertThatThrownBy(() -> EventContract.requireSupported(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported analytics event schema version: 0");
    }

    @Test
    void requireSupported_rejectsNewerVersion() {
        // Act / Assert
        assertThatThrownBy(() -> EventContract.requireSupported(EventContract.CURRENT_VERSION + 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported analytics event schema version: 2");
    }

    @Test
    void requireSupported_rejectsNegativeVersion() {
        // Act / Assert
        assertThatThrownBy(() -> EventContract.requireSupported(-1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported analytics event schema version: -1");
    }
}
