package com.talentradar.user_service.controller;

import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.exception.GlobalExceptionHandler;
import com.talentradar.user_service.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    private MockMvc mockMvc;

    @Mock
    private RoleService roleService;

    @InjectMocks
    private RoleController roleController;

    private final String BASE_URL = "/api/v1/roles";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(roleController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should get all roles successfully")
    void getAllRoles_WithValidRequest_ReturnsRoles() throws Exception {
        // Arrange
        ResponseDto responseDto = ResponseDto.builder()
                .status(true)
                .message("Roles retrieved successfully")
                .data(Map.of("roles", List.of(
                        Map.of("id", "1", "roleName", "ADMIN"),
                        Map.of("id", "2", "roleName", "USER"))))
                .errors(null)
                .build();

        when(roleService.getAllRoles()).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Roles retrieved successfully"))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles.length()").value(2))
                .andExpect(jsonPath("$.data.roles[0].roleName").value("ADMIN"))
                .andExpect(jsonPath("$.data.roles[1].roleName").value("USER"));

        verify(roleService).getAllRoles();
    }

    @Test
    @DisplayName("Should return empty roles list when no roles exist")
    void getAllRoles_WithNoRoles_ReturnsEmptyList() throws Exception {
        // Arrange
        ResponseDto responseDto = ResponseDto.builder()
                .status(true)
                .message("Roles retrieved successfully")
                .data(Map.of("roles", List.of()))
                .errors(null)
                .build();

        when(roleService.getAllRoles()).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Roles retrieved successfully"))
                .andExpect(jsonPath("$.data.roles").isArray())
                .andExpect(jsonPath("$.data.roles.length()").value(0));

        verify(roleService).getAllRoles();
    }

    @Test
    @DisplayName("Should handle service exception gracefully")
    void getAllRoles_WhenServiceThrowsException_ReturnsError() throws Exception {
        // Arrange
        when(roleService.getAllRoles()).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(roleService).getAllRoles();
    }

    @Test
    @DisplayName("Should return error response when service returns error")
    void getAllRoles_WhenServiceReturnsError_ReturnsErrorResponse() throws Exception {
        // Arrange
        ResponseDto responseDto = ResponseDto.builder()
                .status(false)
                .message("Failed to retrieve roles")
                .data(null)
                .errors(List.of(Map.of("error", "Database error occurred")))
                .build();

        when(roleService.getAllRoles()).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(false))
                .andExpect(jsonPath("$.message").value("Failed to retrieve roles"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].error").value("Database error occurred"));

        verify(roleService).getAllRoles();
    }
}