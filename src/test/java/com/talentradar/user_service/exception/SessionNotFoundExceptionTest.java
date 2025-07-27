package com.talentradar.user_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SessionNotFoundExceptionTest {

    @Test
    @DisplayName("Should create SessionNotFoundException with message")
    void shouldCreateWithMessage() {
        // Arrange
        String message = "Session not found";

        // Act
        SessionNotFoundException exception = new SessionNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof AppException);
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
        // Act
        SessionNotFoundException exception = new SessionNotFoundException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertTrue(exception instanceof AppException);
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        SessionNotFoundException exception = new SessionNotFoundException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof AppException);
    }
}