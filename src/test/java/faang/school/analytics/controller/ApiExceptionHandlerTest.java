package faang.school.analytics.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void handleInvalidArgument_returnsBadRequestProblemWithMessage() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("from must be before or equal to to");

        // Act
        ProblemDetail problem = handler.handleInvalidArgument(exception);

        // Assert: title is set explicitly; detail carries the exception message
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isEqualTo("from must be before or equal to to");
        assertThat(problem.getTitle()).isEqualTo("Invalid analytics request");
    }

    @Test
    void handleInvalidArgument_withNullMessage_returnsBadRequestWithDefaultTitle() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException();

        // Act
        ProblemDetail problem = handler.handleInvalidArgument(exception);

        // Assert: title is always set explicitly by the handler, even when the message is null
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getDetail()).isNull();
        assertThat(problem.getTitle()).isEqualTo("Invalid analytics request");
    }
}
