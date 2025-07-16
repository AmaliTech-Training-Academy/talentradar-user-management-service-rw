package com.talentradar.user_service.service.impl;

import com.talentradar.user_service.service.interfaces.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.email.from}")
    private String fromEmail;

    @Value("${notification.email.subject.prefix}")
    private String subjectPrefix;

    @Override
    public void sendRegistrationInvite(String toEmail, String inviteLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject(subjectPrefix + " Registration Invitation");

        String emailContent = """
            You have been invited to join our platform!\n\n            Please click the following link to complete your registration:\n            %s\n\n            This link will expire in 10 minutes.
            """.formatted(inviteLink);

        message.setText(emailContent);
        mailSender.send(message);
    }
}
