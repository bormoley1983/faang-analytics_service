package faang.school.analytics.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserHeaderFilterTest {

    @Mock
    private FilterChain chain;

    @Test
    void doFilter_withUserIdHeader_setsContextAndClearsAfterChain() throws Exception {
        // Arrange
        UserContext userContext = new UserContext();
        UserHeaderFilter filter = new UserHeaderFilter(userContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-user-id", "42");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(request, response, chain);

        // Assert: context is cleared after the chain completes (finally block),
        // so the primitive getter NPEs (documented contract)
        assertThatThrownBy(userContext::getUserId).isInstanceOf(NullPointerException.class);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_withoutUserIdHeader_leavesContextUntouched() throws Exception {
        // Arrange
        UserContext userContext = new UserContext();
        UserHeaderFilter filter = new UserHeaderFilter(userContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Act
        filter.doFilter(request, response, chain);

        // Assert: no header means the context was never set; after clear() it NPEs
        assertThatThrownBy(userContext::getUserId).isInstanceOf(NullPointerException.class);
        verify(chain).doFilter(request, response);
    }

    @Test
    void doFilter_clearsContextEvenWhenChainThrows() throws Exception {
        // Arrange
        UserContext userContext = new UserContext();
        UserHeaderFilter filter = new UserHeaderFilter(userContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-user-id", "7");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doThrow(new ServletException("chain failed")).when(chain).doFilter(request, response);

        // Act / Assert
        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(ServletException.class)
                .hasMessage("chain failed");
        assertThatThrownBy(userContext::getUserId).isInstanceOf(NullPointerException.class);
    }

    @Test
    void doFilter_setsUserIdBeforeChainAndClearsAfter() throws Exception {
        // Arrange
        UserContext userContext = new UserContext();
        UserHeaderFilter filter = new UserHeaderFilter(userContext);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("x-user-id", "99");
        MockHttpServletResponse response = new MockHttpServletResponse();
        doAnswer(invocation -> {
            // Inside the chain the user id must be visible
            assertThat(userContext.getUserId()).isEqualTo(99L);
            return null;
        }).when(chain).doFilter(request, response);

        // Act
        filter.doFilter(request, response, chain);

        // Assert
        verify(chain).doFilter(request, response);
    }
}
