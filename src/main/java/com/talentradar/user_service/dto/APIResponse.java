package com.talentradar.user_service.dto;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@Builder
public class APIResponse<T> {
    private Boolean status;
    private String message;
    private Data<T> data;
    private List<Map<String, String>> errors;

    @Setter
    @Getter
    @AllArgsConstructor
    @Builder
    public static class Data<T> {
        private T data;
    }
}