package com.talentradar.user_service.service;

import com.talentradar.user_service.service.interfaces.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private EmailService emailService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_INVITE_LINK = "https://example.com/register?token=abc123";

    @Test
    @DisplayName("Should call sendRegistrationInvite with correct parameters")
    void sendRegistrationInvite_WithValidParameters_CallsService() {
        // Arrange
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());

        // Act
        emailService.sendRegistrationInvite(TEST_EMAIL, TEST_INVITE_LINK);

        // Assert
        verify(emailService, times(1)).sendRegistrationInvite(TEST_EMAIL, TEST_INVITE_LINK);
    }

    @Test
    @DisplayName("Should call sendRegistrationInvite multiple times")
    void sendRegistrationInvite_MultipleCalls_CallsServiceMultipleTimes() {
        // Arrange
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());

        // Act
        emailService.sendRegistrationInvite(TEST_EMAIL, TEST_INVITE_LINK);
        emailService.sendRegistrationInvite("another@example.com", "https://example.com/register?token=xyz789");

        // Assert
        verify(emailService, times(2)).sendRegistrationInvite(anyString(), anyString());
        verify(emailService, times(1)).sendRegistrationInvite(TEST_EMAIL, TEST_INVITE_LINK);
        verify(emailService, times(1)).sendRegistrationInvite("another@example.com",
                "https://example.com/register?token=xyz789");
    }

    @Test
    @DisplayName("Should not call sendRegistrationInvite when not invoked")
    void sendRegistrationInvite_NotInvoked_DoesNotCallService() {
        // Act - do nothing

        // Assert
        verify(emailService, never()).sendRegistrationInvite(anyString(), anyString());
    }

    @Test
    @DisplayName("Should call sendRegistrationInvite with empty email")
    void sendRegistrationInvite_WithEmptyEmail_CallsService() {
        // Arrange
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());

        // Act
        emailService.sendRegistrationInvite("", TEST_INVITE_LINK);

        // Assert
        verify(emailService, times(1)).sendRegistrationInvite("", TEST_INVITE_LINK);
    }

    @Test
    @DisplayName("Should call sendRegistrationInvite with empty invite link")
    void sendRegistrationInvite_WithEmptyInviteLink_CallsService() {
        // Arrange
        doNothing().when(emailService).sendRegistrationInvite(anyString(), anyString());

        // Act
        emailService.sendRegistrationInvite(TEST_EMAIL, "");

        // Assert
        verify(emailService, times(1)).sendRegistrationInvite(TEST_EMAIL, "");
    }

    @Test
    @DisplayName("Should call sendRegistrationInvite with null parameters")
    void sendRegistrationInvite_WithNullParameters_CallsService() {
        // Arrange
        doNothing().when(emailService).sendRegistrationInvite(any(), any());

        // Act
        emailService.sendRegistrationInvite(null, null);

        // Assert
        verify(emailService, times(1)).sendRegistrationInvite(null, null);
    }
}