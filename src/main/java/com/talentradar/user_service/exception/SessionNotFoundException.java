package com.talentradar.user_service.exception;

public class SessionNotFoundException extends AppException{
    public SessionNotFoundException(String message) {
        super(message);
    }
}
