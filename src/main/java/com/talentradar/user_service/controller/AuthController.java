package com.talentradar.user_service.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.user_service.dto.LoginRequestDto;
import com.talentradar.user_service.service.AuthenticationService;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

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
}
