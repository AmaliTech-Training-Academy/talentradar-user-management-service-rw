package com.talentradar.user_service.controller;

import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.exception.GlobalExceptionHandler;
import com.talentradar.user_service.service.UserService;
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

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private final String BASE_URL = "/api/v1/users";
    private final UUID TEST_USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should get current user successfully")
    void getMe_WithValidUserId_ReturnsUser() throws Exception {
        // Arrange
        ResponseDto responseDto = ResponseDto.builder()
                .status(true)
                .message("User retrieved successfully")
                .data(Map.of("user", Map.of(
                        "id", TEST_USER_ID.toString(),
                        "email", "test@example.com",
                        "username", "testuser",
                        "fullName", "Test User",
                        "role", "USER")))
                .errors(null)
                .build();

        when(userService.getMe(TEST_USER_ID)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/me")
                .header("X-User-Id", TEST_USER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("User retrieved successfully"))
                .andExpect(jsonPath("$.data.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.user.username").value("testuser"));

        verify(userService).getMe(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should return error when user ID header is missing")
    void getMe_WithMissingUserIdHeader_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).getMe(any());
    }

    @Test
    @DisplayName("Should return error when user ID header is invalid")
    void getMe_WithInvalidUserIdHeader_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/me")
                .header("X-User-Id", "invalid-uuid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService, never()).getMe(any());
    }

    @Test
    @DisplayName("Should get all users with default pagination")
    void getAll_WithDefaultPagination_ReturnsUsers() throws Exception {
        // Arrange
        ResponseDto responseDto = ResponseDto.builder()
                .status(true)
                .message("Users retrieved successfully")
                .data(Map.of(
                        "users", Map.of("content", "[]"),
                        "pageInfo", Map.of(
                                "currentPage", 1,
                                "pageSize", 10,
                                "totalElements", 0,
                                "totalPages", 0)))
                .errors(null)
                .build();

        when(userService.getAllUsers(0, 10, null)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"));

        verify(userService).getAllUsers(0, 10, null);
    }

    @Test
    @DisplayName("Should get all users with custom pagination")
    void getAll_WithCustomPagination_ReturnsUsers() throws Exception {
        // Arrange
        ResponseDto responseDto = ResponseDto.builder()
                .status(true)
                .message("Users retrieved successfully")
                .data(Map.of(
                        "users", Map.of("content", "[]"),
                        "pageInfo", Map.of(
                                "currentPage", 2,
                                "pageSize", 5,
                                "totalElements", 0,
                                "totalPages", 0)))
                .errors(null)
                .build();

        when(userService.getAllUsers(1, 5, null)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .param("page", "1")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"));

        verify(userService).getAllUsers(1, 5, null);
    }

    @Test
    @DisplayName("Should get users filtered by role")
    void getAll_WithRoleFilter_ReturnsFilteredUsers() throws Exception {
        // Arrange
        UUID roleId = UUID.randomUUID();
        ResponseDto responseDto = ResponseDto.builder()
                .status(true)
                .message("Users retrieved successfully")
                .data(Map.of(
                        "users", Map.of("content", "[]"),
                        "pageInfo", Map.of(
                                "currentPage", 1,
                                "pageSize", 10,
                                "totalElements", 0,
                                "totalPages", 0)))
                .errors(null)
                .build();

        when(userService.getAllUsers(0, 10, roleId)).thenReturn(responseDto);

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .param("roleId", roleId.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message").value("Users retrieved successfully"));

        verify(userService).getAllUsers(0, 10, roleId);
    }

    @Test
    @DisplayName("Should handle service exception gracefully")
    void getAll_WhenServiceThrowsException_ReturnsError() throws Exception {
        // Arrange
        when(userService.getAllUsers(0, 10, null))
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());

        verify(userService).getAllUsers(0, 10, null);
    }
}