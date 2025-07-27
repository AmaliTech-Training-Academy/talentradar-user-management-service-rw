package com.talentradar.user_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppExceptionTest {

    @Test
    @DisplayName("Should create AppException with message")
    void shouldCreateWithMessage() {
        // Arrange
        String message = "Application error occurred";

        // Act
        AppException exception = new AppException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
        // Act
        AppException exception = new AppException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should handle empty message")
    void shouldHandleEmptyMessage() {
        // Arrange
        String message = "";

        // Act
        AppException exception = new AppException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }
}