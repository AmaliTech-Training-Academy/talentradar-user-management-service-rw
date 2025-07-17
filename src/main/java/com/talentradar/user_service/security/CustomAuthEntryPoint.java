package com.talentradar.user_service.security;

import java.util.List;
import java.util.Map;

import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentradar.user_service.dto.ResponseDto;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            org.springframework.security.core.AuthenticationException authException)
            throws java.io.IOException, ServletException {
        ResponseDto apiResponse = ResponseDto.builder()
                .status(false)
                .message("Unauthorized")
                .data(null)
                .errors(List.of(Map.of("message", "Unauthorized access. Please log in.")))
                .build();

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String json = objectMapper.writeValueAsString(apiResponse);
        response.getWriter().write(json);
    }
}
