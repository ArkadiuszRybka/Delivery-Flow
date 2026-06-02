package com.deliveryflow.tracking.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    @DisplayName("should return 404 ProblemDetail with orderId in detail")
    void shouldReturn404ForTrackingNotFoundException() {
        UUID orderId = UUID.randomUUID();
        var ex = new TrackingNotFoundException(orderId);

        ProblemDetail result = handler.handleTrackingNotFound(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(result.getTitle()).isEqualTo("Tracking Not Found");
        assertThat(result.getDetail()).contains(orderId.toString());
    }

    @Test
    @DisplayName("should return 400 ProblemDetail with field error messages")
    void shouldReturn400ForValidationException() throws NoSuchMethodException {
        var bindingResult = new BeanPropertyBindingResult(new Object(), "createTrackingEntryRequest");
        bindingResult.addError(new FieldError("createTrackingEntryRequest", "orderId", "must not be null"));

        var method = Object.class.getDeclaredMethod("toString");
        var ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, -1),
                bindingResult
        );

        ProblemDetail result = handler.handleValidation(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(result.getTitle()).isEqualTo("Validation Failed");
        assertThat(result.getDetail()).contains("must not be null");
    }

    @Test
    @DisplayName("should return 500 ProblemDetail without exposing internal exception message")
    void shouldReturn500WithoutExposingInternals() {
        var ex = new RuntimeException("database connection lost");

        ProblemDetail result = handler.handleGeneric(ex);

        assertThat(result.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(result.getTitle()).isEqualTo("Internal Server Error");
        assertThat(result.getDetail()).doesNotContain("database connection lost");
    }
}
