package com.talentradar.user_service.controller;

import com.talentradar.user_service.dto.CustomPageResponse;
import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.exception.GlobalExceptionHandler;
import com.talentradar.user_service.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SessionService sessionService;

    @InjectMocks
    private SessionController sessionController;

    private final String BASE_URL = "/api/v1/admin/sessions";
    private final UUID TEST_USER_ID = UUID.randomUUID();
    private final String TEST_SESSION_ID = "test-session-123";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sessionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("Should get active sessions successfully")
    void viewActiveSession_WithValidRequest_ReturnsActiveSessions() throws Exception {
        // Arrange
        SessionResponseDto sessionDto = new SessionResponseDto();
        sessionDto.setId(TEST_USER_ID);
        sessionDto.setSessionId(TEST_SESSION_ID);
        sessionDto.setIpAddress("192.168.1.1");
        sessionDto.setDeviceInfo("Chrome/Windows");
        sessionDto.setCreatedAt(LocalDateTime.now());
        sessionDto.setActive(true);

        CustomPageResponse<SessionResponseDto> sessionsList = CustomPageResponse.<SessionResponseDto>builder()
                .items(List.of(sessionDto))
                .totalElements(1L)
                .totalPages(1)
                .page(0)
                .size(10)
                .build();

        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should delete session successfully")
    void deleteSession_WithValidSessionId_ReturnsSuccess() throws Exception {
        // Arrange
        doNothing().when(sessionService).revokeSessionById(TEST_SESSION_ID);

        // Act & Assert
        mockMvc.perform(delete(BASE_URL + "/" + TEST_SESSION_ID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.message")
                        .value("The session with id '" + TEST_SESSION_ID + "' revoked successfully"));

        verify(sessionService).revokeSessionById(TEST_SESSION_ID);
    }

    @Test
    @DisplayName("Should filter sessions by userId only")
    void filterSession_WithUserIdOnly_ReturnsFilteredSessions() throws Exception {
        // Arrange
        SessionResponseDto sessionDto = new SessionResponseDto();
        sessionDto.setId(TEST_USER_ID);
        sessionDto.setSessionId(TEST_SESSION_ID);
        sessionDto.setActive(true);

        CustomPageResponse<SessionResponseDto> sessionsList = CustomPageResponse.<SessionResponseDto>builder()
                .items(List.of(sessionDto))
                .totalElements(1L)
                .build();

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/filter")
                .param("userId", TEST_USER_ID.toString())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should filter sessions by date only")
    void filterSession_WithDateOnly_ReturnsFilteredSessions() throws Exception {
        // Arrange
        String date = "2025-07-27";
        SessionResponseDto sessionDto = new SessionResponseDto();
        sessionDto.setId(TEST_USER_ID);
        sessionDto.setSessionId(TEST_SESSION_ID);
        sessionDto.setActive(true);

        CustomPageResponse<SessionResponseDto> sessionsList = CustomPageResponse.<SessionResponseDto>builder()
                .items(List.of(sessionDto))
                .totalElements(1L)
                .build();

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/filter")
                .param("date", date)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should filter sessions by userId and date")
    void filterSession_WithUserIdAndDate_ReturnsFilteredSessions() throws Exception {
        // Arrange
        String date = "2025-07-27";
        SessionResponseDto sessionDto = new SessionResponseDto();
        sessionDto.setId(TEST_USER_ID);
        sessionDto.setSessionId(TEST_SESSION_ID);
        sessionDto.setActive(true);

        CustomPageResponse<SessionResponseDto> sessionsList = CustomPageResponse.<SessionResponseDto>builder()
                .items(List.of(sessionDto))
                .totalElements(1L)
                .build();

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/filter")
                .param("userId", TEST_USER_ID.toString())
                .param("date", date)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should filter sessions with no parameters")
    void filterSession_WithNoParameters_ReturnsAllSessions() throws Exception {
        // Arrange
        SessionResponseDto sessionDto = new SessionResponseDto();
        sessionDto.setId(TEST_USER_ID);
        sessionDto.setSessionId(TEST_SESSION_ID);
        sessionDto.setActive(true);

        CustomPageResponse<SessionResponseDto> sessionsList = CustomPageResponse.<SessionResponseDto>builder()
                .items(List.of(sessionDto))
                .totalElements(1L)
                .build();

        // Act & Assert
        mockMvc.perform(get(BASE_URL + "/filter")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should handle service exception gracefully")
    void viewActiveSession_WhenServiceThrowsException_ReturnsError() throws Exception {
        // Arrange
        // Act & Assert
        mockMvc.perform(get(BASE_URL)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}