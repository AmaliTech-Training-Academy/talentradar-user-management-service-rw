package com.talentradar.user_service.exception;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.talentradar.user_service.dto.APIResponse;
import com.talentradar.user_service.dto.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)

    public ResponseEntity<APIResponse<?>> handleUserNotFoundException(UserNotFoundException ex) {
        APIResponse<?> response = APIResponse.builder()
                .status(false)
                .message("Fetching User Failed")
                .data(null)
                .errors(List.of(Map.of("message", ex.getMessage())))
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<APIResponse<?>> handleInvalidCredentials(BadCredentialsException ex) {
        APIResponse<?> response = APIResponse.builder()
                .status(false)
                .message("Login Failed")
                .errors(List.of(Map.of("message", ex.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }
}
