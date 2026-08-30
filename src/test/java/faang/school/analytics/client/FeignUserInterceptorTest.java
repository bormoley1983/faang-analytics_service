package faang.school.analytics.client;

import faang.school.analytics.config.UserContext;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class FeignUserInterceptorTest {

    private final UserContext userContext = new UserContext();
    private final FeignUserInterceptor interceptor = new FeignUserInterceptor(userContext);

    @Test
    void apply_addsUserIdHeaderToTemplate() {
        // Arrange
        RequestTemplate template = new RequestTemplate();
        userContext.setUserId(42L);

        // Act
        interceptor.apply(template);

        // Assert
        Collection<String> values = template.headers().get("x-user-id");
        assertThat(values).containsExactly("42");
    }

    @Test
    void apply_addsHeaderForDifferentUserId() {
        // Arrange
        RequestTemplate template = new RequestTemplate();
        userContext.setUserId(123456789L);

        // Act
        interceptor.apply(template);

        // Assert
        Collection<String> values = template.headers().get("x-user-id");
        assertThat(values).containsExactly("123456789");
    }
}
