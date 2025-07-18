package com.talentradar.user_service.exception;

public class UnauthorizedException extends AppException{
    public UnauthorizedException(String message) {
        super(message);
    }
}
