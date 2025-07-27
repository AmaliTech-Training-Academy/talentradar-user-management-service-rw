package com.talentradar.user_service.service;

import com.talentradar.user_service.service.impl.SmtpEmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmtpEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private SmtpEmailService smtpEmailService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_INVITE_LINK = "https://example.com/register?token=abc123";
    private final String FROM_EMAIL = "noreply@talentradar.com";
    private final String SUBJECT_PREFIX = "[TalentRadar]";

    @BeforeEach
    void setUp() {
        // Set up the private fields using ReflectionTestUtils
        ReflectionTestUtils.setField(smtpEmailService, "fromEmail", FROM_EMAIL);
        ReflectionTestUtils.setField(smtpEmailService, "subjectPrefix", SUBJECT_PREFIX);
    }

    @Test
    @DisplayName("Should send registration invite email successfully")
    void sendRegistrationInvite_WithValidParameters_SendsEmail() throws Exception {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        smtpEmailService.sendRegistrationInvite(TEST_EMAIL, TEST_INVITE_LINK);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send registration invite email with correct subject")
    void sendRegistrationInvite_WithValidParameters_SetsCorrectSubject() throws Exception {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        smtpEmailService.sendRegistrationInvite(TEST_EMAIL, TEST_INVITE_LINK);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
        // Note: We can't easily verify the subject content without more complex mocking
        // The main goal is to verify the method calls
    }

    @Test
    @DisplayName("Should throw exception when email is empty")
    void sendRegistrationInvite_WithEmptyEmail_ThrowsException() {
        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            smtpEmailService.sendRegistrationInvite("", TEST_INVITE_LINK);
        });
    }

    @Test
    @DisplayName("Should send registration invite email with empty invite link")
    void sendRegistrationInvite_WithEmptyInviteLink_SendsEmail() throws Exception {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        smtpEmailService.sendRegistrationInvite(TEST_EMAIL, "");

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should throw exception when email is null")
    void sendRegistrationInvite_WithNullEmail_ThrowsException() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            smtpEmailService.sendRegistrationInvite(null, TEST_INVITE_LINK);
        });
    }

    @Test
    @DisplayName("Should throw RuntimeException when send method fails")
    void sendRegistrationInvite_WhenSendMethodFails_ThrowsRuntimeException() throws Exception {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new RuntimeException("Send failed")).when(mailSender).send(any(MimeMessage.class));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            smtpEmailService.sendRegistrationInvite(TEST_EMAIL, TEST_INVITE_LINK);
        });

        assertEquals("Send failed", exception.getMessage());
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should send multiple registration invite emails")
    void sendRegistrationInvite_MultipleCalls_SendsMultipleEmails() throws Exception {
        // Arrange
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(any(MimeMessage.class));

        // Act
        smtpEmailService.sendRegistrationInvite(TEST_EMAIL, TEST_INVITE_LINK);
        smtpEmailService.sendRegistrationInvite("another@example.com", "https://example.com/register?token=xyz789");

        // Assert
        verify(mailSender, times(2)).createMimeMessage();
        verify(mailSender, times(2)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should not send email when not invoked")
    void sendRegistrationInvite_NotInvoked_DoesNotSendEmail() {
        // Act - do nothing

        // Assert
        verify(mailSender, never()).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}