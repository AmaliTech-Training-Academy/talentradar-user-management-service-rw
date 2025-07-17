package com.talentradar.user_service.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.dto.ErrorResponse;
import com.talentradar.user_service.dto.LoginResponseDto;
import org.springframework.web.context.request.WebRequest;
import com.talentradar.user_service.dto.UserNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UserNotFoundException.class)

    public ResponseEntity<ResponseDto> handleUserNotFoundException(UserNotFoundException ex) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Fetching User Failed")
                .data(null)
                .errors(List.of(Map.of("message", ex.getMessage())))
                .build();

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Handle validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<Map<String, String>> errors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = error instanceof FieldError ? ((FieldError) error).getField()
                            : error.getObjectName();
                    String errorMessage = error.getDefaultMessage();
                    return Map.of("field", fieldName, "message", errorMessage);
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse("Validation failed", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParams(MissingServletRequestParameterException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle invalid credentials exception
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseDto> handleInvalidCredentials(BadCredentialsException ex) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Login Failed")
                .errors(List.of(Map.of("message", ex.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }


    // handle user is not found exception response
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFound(
            UserNotFoundException exception, WebRequest webRequest) {
        Map<String, Object> body = new HashMap<>();
        body.put("code", HttpStatus.NOT_FOUND.value());
        body.put("timestamp", LocalDateTime.now());
        body.put("message", exception.getMessage());
        body.put("path", webRequest.getContextPath());
        body.put("sessionId", webRequest.getSessionId());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Handle resource not found exception
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    // Handle resource already exists exception
    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    // Handle invalid token exception
    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidTokenException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle illegal state exceptions (e.g., email already in use)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        ErrorResponse errorResponse = new ErrorResponse(ex.getMessage(), null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Handle all other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse("An unexpected error occurred", null);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);

    }
}
