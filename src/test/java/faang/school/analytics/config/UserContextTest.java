package faang.school.analytics.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserContextTest {

    @Test
    void setAndGetUserId_roundTrips() {
        // Arrange
        UserContext userContext = new UserContext();

        // Act
        userContext.setUserId(42L);

        // Assert
        assertThat(userContext.getUserId()).isEqualTo(42L);
    }

    @Test
    void clear_removesUserId() {
        // Arrange
        UserContext userContext = new UserContext();
        userContext.setUserId(42L);

        // Act
        userContext.clear();

        // Assert: ThreadLocal is empty again, so the primitive getter NPEs (documented contract)
        org.assertj.core.api.Assertions.assertThatThrownBy(userContext::getUserId)
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void setUserId_overwritesPreviousValue() {
        // Arrange
        UserContext userContext = new UserContext();
        userContext.setUserId(1L);

        // Act
        userContext.setUserId(2L);

        // Assert
        assertThat(userContext.getUserId()).isEqualTo(2L);
    }
}
