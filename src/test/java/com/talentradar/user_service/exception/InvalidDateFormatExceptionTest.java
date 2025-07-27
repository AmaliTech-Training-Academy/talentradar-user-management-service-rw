package com.talentradar.user_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InvalidDateFormatExceptionTest {

    @Test
    @DisplayName("Should create InvalidDateFormatException with message")
    void shouldCreateWithMessage() {
        // Arrange
        String message = "Invalid date format provided";

        // Act
        InvalidDateFormatException exception = new InvalidDateFormatException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof AppException);
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
        // Act
        InvalidDateFormatException exception = new InvalidDateFormatException(null);

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
        InvalidDateFormatException exception = new InvalidDateFormatException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof AppException);
    }
}