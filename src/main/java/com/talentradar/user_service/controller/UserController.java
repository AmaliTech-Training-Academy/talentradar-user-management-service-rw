package com.talentradar.user_service.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.service.UserService;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Define user-related endpoints here
    @GetMapping("/me")
    public ResponseEntity<ResponseDto> getMe(@RequestHeader("X-User-Id") String userIdFromHeader) {
        // Get user id from header X-User-Id
        UUID userId = UUID.fromString(userIdFromHeader);

        // Load User from database
        ResponseDto loginResponseDto = userService.getMe(userId);

        return ResponseEntity.ok(loginResponseDto);
    }

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        ResponseDto response = userService.getAllUsers(page, size);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
