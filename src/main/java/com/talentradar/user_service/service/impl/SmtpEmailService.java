package com.talentradar.user_service.service.impl;

import com.talentradar.user_service.service.interfaces.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@Profile("prod")
@RequiredArgsConstructor
public class SmtpEmailService implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.notification.email.from}")
    private String fromEmail;

    @Value("${app.notification.email.subject.prefix}")
    private String subjectPrefix;

    @Override
    public void sendRegistrationInvite(String toEmail, String inviteLink) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subjectPrefix + " Registration Invitation");

            String htmlContent = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Registration Invitation</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background-color: #f4f4f4;
                            margin: 0;
                            padding: 20px;
                        }
                        .container {
                            background-color: #ffffff;
                            padding: 30px;
                            border-radius: 8px;
                            box-shadow: 0 4px 8px rgba(0,0,0,0.1);
                            max-width: 600px;
                            margin: auto;
                        }
                        .button {
                            background-color: #007bff;
                            color: white;
                            padding: 12px 25px;
                            text-align: center;
                            text-decoration: none;
                            display: inline-block;
                            font-size: 16px;
                            border-radius: 5px;
                            font-weight: bold;
                        }
                        p {
                            font-size: 16px;
                            line-height: 1.5;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>You're Invited!</h2>
                        <p>You have been invited to join our platform. Please click the button below to complete your registration:</p>
                        <p><a href=\"%s\" class=\"button\">Complete Registration</a></p>
                        <p>This link will expire in 10 minutes.</p>
                        <hr>
                        <p><small>If you did not request this invitation, please disregard this email.</small></p>
                    </div>
                </body>
                </html>
                """.formatted(inviteLink);

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            // Consider a more robust error handling strategy
            throw new RuntimeException("Failed to send email", e);
        }
    }
}