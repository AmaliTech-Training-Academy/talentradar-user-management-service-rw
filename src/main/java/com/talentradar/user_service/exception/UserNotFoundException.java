package com.talentradar.user_service.exception;

public class UserNotFoundException extends AppException{
    public UserNotFoundException(String message) {
        super(message);
    }
}
