package com.talentradar.user_service.exception;

public class NotFoundUserException extends AppException{
    public NotFoundUserException(String message) {
        super(message);
    }
}
