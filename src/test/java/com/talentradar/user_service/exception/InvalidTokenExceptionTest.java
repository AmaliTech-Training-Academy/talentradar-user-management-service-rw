package com.talentradar.user_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidTokenExceptionTest {

    @Test
    @DisplayName("Should create InvalidTokenException with message")
    void shouldCreateWithMessage() {
        // Arrange
        String message = "Invalid token provided";

        // Act
        InvalidTokenException exception = new InvalidTokenException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create InvalidTokenException with message and cause")
    void shouldCreateWithMessageAndCause() {
        // Arrange
        String message = "Invalid token provided";
        Throwable cause = new RuntimeException("Root cause");

        // Act
        InvalidTokenException exception = new InvalidTokenException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
        // Act
        InvalidTokenException exception = new InvalidTokenException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null cause")
    void shouldHandleNullCause() {
        // Arrange
        String message = "Invalid token provided";

        // Act
        InvalidTokenException exception = new InvalidTokenException(message, null);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}