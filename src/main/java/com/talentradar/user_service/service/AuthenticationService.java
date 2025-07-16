package com.talentradar.user_service.service;

import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.talentradar.user_service.dto.LoginRequestDto;
import com.talentradar.user_service.dto.LoginResponseDto;
import com.talentradar.user_service.dto.UserDto;
import com.talentradar.user_service.model.CustomUserDetails;
import com.talentradar.user_service.security.JwtUtils;

@Service
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    public Map<String, Object> login(LoginRequestDto loginRequest) {

        // Authenticate user with username or email
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()));

        // Get authenticated user details
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        // Generate JWT token
        String token = jwtUtils.generateJwtTokenFromEmail(userDetails);

        UserDto userDto = UserDto.builder()
                .id(userDetails.getUserId())
                .email(userDetails.getEmail())
                .username(userDetails.getUsername())
                .fullName(userDetails.getUser().getFullName())
                .roleName(userDetails.getRoleName())
                .build();

        // Create and return login response
        LoginResponseDto loginResponseDto = LoginResponseDto.builder().status(true).message("Login successful")
                .errors(
                        null)
                .data(LoginResponseDto.Data.builder().user(userDto).build())
                .build();

        return Map.of("token", token, "loginResponse", loginResponseDto);

    }
}
