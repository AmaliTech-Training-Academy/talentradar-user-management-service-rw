package com.talentradar.user_service.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAlreadyExistsExceptionTest {

    @Test
    @DisplayName("Should create ResourceAlreadyExistsException with message")
    void shouldCreateWithMessage() {
        // Arrange
        String message = "User already exists";

        // Act
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException(message);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
    }

    @Test
    @DisplayName("Should create ResourceAlreadyExistsException with message and cause")
    void shouldCreateWithMessageAndCause() {
        // Arrange
        String message = "User already exists";
        Throwable cause = new RuntimeException("Root cause");

        // Act
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException(message, cause);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Should handle null message")
    void shouldHandleNullMessage() {
        // Act
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException(null);

        // Assert
        assertNotNull(exception);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Should handle null cause")
    void shouldHandleNullCause() {
        // Arrange
        String message = "User already exists";

        // Act
        ResourceAlreadyExistsException exception = new ResourceAlreadyExistsException(message, null);

        // Assert
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }
}