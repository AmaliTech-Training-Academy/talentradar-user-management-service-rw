package com.talentradar.user_service.service;

import com.talentradar.user_service.service.interfaces.EmailService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Profile("!prod")
public class ConsoleEmailService implements EmailService {

    @Override
    public void sendRegistrationInvite(String toEmail, String inviteLink) {
        String emailContent = """
            ============================================
            Registration Invitation
            ============================================
            
            You have been invited to join our platform!
            
            Please click the following link to complete your registration:
            %s
            
            This link will expire in 10 minutes.
            ============================================
            """.formatted(inviteLink);
            
        log.info("Sending registration email to: {}\n{}", toEmail, emailContent);
    }
}
