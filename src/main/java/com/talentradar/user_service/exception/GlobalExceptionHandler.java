package com.talentradar.user_service.exception;

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
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.talentradar.user_service.dto.ErrorResponse;
import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.dto.UserNotFoundException;

import io.swagger.v3.oas.annotations.Hidden;

@RestControllerAdvice
@Hidden
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
    public ResponseEntity<?> handleUnexpectedException(Exception exception, WebRequest webRequest) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Unexpected Exception")
                .errors(List.of(Map.of("message", exception.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // handle user is not found exception
    @ExceptionHandler(NotFoundUserException.class)
    public ResponseEntity<?> handleUserNotFound(
            UserNotFoundException exception) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("UserNotFound")
                .errors(List.of(Map.of("message", exception.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // handle invalid date format exception
    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<?> handleUserInvalidDateFormat(
            InvalidDateFormatException exception) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Invalid date format")
                .errors(List.of(Map.of("message", exception.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // handle session is not found exception response
    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<?> handleSessionNotFound(
            SessionNotFoundException exception) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("SessionId doesnt exist")
                .errors(List.of(Map.of("message", exception.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<?> handleUnauthorizedException(UnauthorizedException unauthorizedException) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Unauthorized Exception")
                .errors(List.of(Map.of("message", unauthorizedException.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // handle any unexpected Error
    @ExceptionHandler(Error.class)
    public ResponseEntity<?> handleError(Error error) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Internal Server Error")
                .errors(List.of(Map.of("message", error.getMessage())))
                .data(null)
                .build();

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // handle no url mapped
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handle404(NoHandlerFoundException exception) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("Page/url no found")
                .errors(List.of(Map.of("message", exception.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Handle no internet connection
    @ExceptionHandler(NoInternetConnectionException.class)
    public ResponseEntity<?> noInternetConnection(NoInternetConnectionException exception) {
        ResponseDto response = ResponseDto.builder()
                .status(false)
                .message("No internet connection")
                .errors(List.of(Map.of("message", exception.getMessage())))
                .data(null)
                .build();
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

}
