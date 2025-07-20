package com.talentradar.user_service.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.talentradar.user_service.dto.CompleteRegistrationRequest;
import com.talentradar.user_service.dto.InviteUserRequest;
import com.talentradar.user_service.dto.LoginRequestDto;
import com.talentradar.user_service.dto.UserResponse;
import com.talentradar.user_service.exception.InvalidTokenException;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.service.AuthenticationService;
import com.talentradar.user_service.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<Object> signin(@RequestBody LoginRequestDto loginRequest) {
        Map<String, Object> loginResponse = authService.login(loginRequest);
        // Put token in httpOnly cookie
        HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.SET_COOKIE, "token=" + loginResponse.get("token")
                + "; HttpOnly; Path=/; Max-Age=3600; secure=false; SameSite=Strict");

        return ResponseEntity.ok()
                .headers(headers)
                .body(loginResponse.get("loginResponse"));

    }

    /**
     * Initiates the user registration process.
     * Admin creates a user with email and role, and an invite is sent to the user's
     * email.
     */
    @PostMapping("/invite")
    public ResponseEntity<UserResponse> initiateRegistration(@Valid @RequestBody InviteUserRequest inviteRequest) {
        User newUser = userService.initiateRegistration(inviteRequest);
        return new ResponseEntity<>(UserResponse.fromUser(newUser), HttpStatus.CREATED);
    }

    /**
     * Completes the user registration process.
     * User provides full name and password to complete registration.
     */
    @PatchMapping("/complete-registration")
    public ResponseEntity<UserResponse> completeRegistration(
            @RequestParam("token") String token,
            @Valid @RequestBody CompleteRegistrationRequest request) {

        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidTokenException("Password and confirmation do not match");
        }

        User user = userService.completeRegistration(token, request);
        return ResponseEntity.ok(UserResponse.fromUser(user));
    }

    /**
     * Validates a registration token.
     * Can be used to check if a registration token is valid before showing the
     * registration form.
     */
    @GetMapping("/validate-registration-token")
    public ResponseEntity<Map<String, String>> validateRegistrationToken(@RequestParam("token") String token) {
        User user = userService.validateRegistrationToken(token);
        return ResponseEntity.ok(Map.of(
                "email", user.getEmail(),
                "status", "VALID"));
    }
}
