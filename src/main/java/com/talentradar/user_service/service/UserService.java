package com.talentradar.user_service.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.talentradar.user_service.dto.LoginResponseDto;
import com.talentradar.user_service.dto.UserDto;
import com.talentradar.user_service.dto.UserNotFoundException;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponseDto getMe(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        UserDto userDto = UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .role(user.getRole().getRoleName())
                .build();

        // Create and return
        LoginResponseDto loginResponseDto = LoginResponseDto.builder().status(true)
                .message("User information retrieved successfully")
                .errors(
                        null)
                .data(LoginResponseDto.Data.builder().user(userDto).build())
                .build();
        return loginResponseDto;
    }

}
