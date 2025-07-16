package com.talentradar.user_service.exception;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.talentradar.user_service.dto.LoginResponseDto;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Handle invalid credentials exception
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<LoginResponseDto> handleInvalidCredentials(BadCredentialsException ex) {
        Map<String, String> errorDetails = Map.of("message", "Invalid credentials");
        LoginResponseDto errorResponse = LoginResponseDto.builder()
                .status(false)
                .message("Login Failed")
                .errors(List.of(errorDetails))
                .build();
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}
