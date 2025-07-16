package com.talentradar.user_service.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Setter
@Getter
public class LoginResponseDto {
    private Boolean status;
    private String message;
    private Data data;
    private List<Map<String, String>> errors;

    @Setter
    @Getter
    @AllArgsConstructor
    @Builder
    public static class Data {
        private UserDto user;
    }
}
