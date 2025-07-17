package com.talentradar.user_service.dto;

import java.util.UUID;

import org.springframework.data.domain.Page;

import com.talentradar.user_service.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserDto {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private String role;

    public static Page<UserDto> fromPage(Page<User> usersPage) {
        return usersPage.map(user -> UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().getRoleName())
                .build());
    }

}
