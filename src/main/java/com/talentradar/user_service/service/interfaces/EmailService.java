package com.talentradar.user_service.service.interfaces;

public interface EmailService {
    void sendRegistrationInvite(String toEmail, String inviteLink);
}
