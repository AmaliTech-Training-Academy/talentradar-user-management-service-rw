package com.talentradar.user_service.dto;

import java.util.UUID;

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
}
