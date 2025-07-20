package com.talentradar.user_service;

import com.talentradar.user_service.dto.CustomPageResponse;
import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.exception.SessionNotFoundException;
import com.talentradar.user_service.mapper.SessionMapper;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.repository.UserSessionRepository;
import com.talentradar.user_service.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionServiceRevokeTests {

    @Mock
    private UserSessionRepository userSessionRepository;
    @Mock
    SessionMapper sessionMapper;

    @InjectMocks
    private SessionService sessionService;

    @Test
    void testGetActiveSessions_returnsMappedDtos() {
        // Given
        Session session = Session.builder()
                .id(UUID.randomUUID())
                .sessionId("abc123")
                .ipAddress("192.168.0.1")
                .deviceInfo("Chrome")
                .createdAt(LocalDateTime.now())
                .isActive(true)
                .build();

        SessionResponseDto responseDto = new SessionResponseDto();
        responseDto.setSessionId("abc123");

        Page<Session> sessionPage = new PageImpl<>(List.of(session));

        // match the actual repository method used in your service
        when(userSessionRepository.findAllByIsActiveTrue (any(Pageable.class))).thenReturn(sessionPage);
        when(sessionMapper.toDto(session)).thenReturn(responseDto);

        // When
        CustomPageResponse<SessionResponseDto> result = sessionService.getActiveSessions(PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getSessionId()).isEqualTo("abc123");

        // match the method you mocked above
        verify(userSessionRepository).findAllByIsActiveTrue(any(Pageable.class));
        verify(sessionMapper).toDto(session);
    }

    @Test
    void testRevokeSessionById_whenSessionExists_shouldInvalidateAndDelete() {
        // Given
        String sessionId = "abc-123";
        Session session = new Session(); // session Entity
        HttpSession httpSession = mock(HttpSession.class);

        when(userSessionRepository.findBySessionId(sessionId)).thenReturn(Optional.of(session));
        // When
        sessionService.revokeSessionById(sessionId, httpSession);

        // Then
        verify(userSessionRepository).findBySessionId(sessionId);
        verify(httpSession).invalidate();
        verify(userSessionRepository).deleteBySessionId(sessionId);
    }

    @Test
    void testRevokeSessionById_whenSessionNotFound_shouldThrowException() {
        String sessionId = "not-found";
        HttpSession httpSession = mock(HttpSession.class);

        when(userSessionRepository.findBySessionId(sessionId)).thenReturn(Optional.empty());

        Assertions.assertThrows(SessionNotFoundException.class, () -> {
            sessionService.revokeSessionById(sessionId, httpSession);
        });

        verify(httpSession, never()).invalidate();
        verify(userSessionRepository, never()).deleteBySessionId(any());
    }
}
