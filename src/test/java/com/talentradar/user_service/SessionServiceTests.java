package com.talentradar.user_service;

import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.mapper.SessionMapper;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.repository.UserSessionRepository;
import com.talentradar.user_service.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SessionServiceTests {
    @Mock
    private UserSessionRepository sessionRepository;

    @Mock
    private SessionMapper sessionMapper;

    @InjectMocks
    private SessionService sessionService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

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

        // 🔁 UPDATED: match the actual repository method used in your service
        when(sessionRepository.findAllByIsActiveTrue (any(Pageable.class))).thenReturn(sessionPage);
        when(sessionMapper.toDto(session)).thenReturn(responseDto);

        // When
        Page<SessionResponseDto> result = sessionService.getActiveSessions(PageRequest.of(0, 10));

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getSessionId()).isEqualTo("abc123");

        // 🔁 UPDATED: match the method you mocked above
        verify(sessionRepository).findAllByIsActiveTrue(any(Pageable.class));
        verify(sessionMapper).toDto(session);
    }


    @Test
    void testRevokeSessionById_whenSessionNotFound_shouldThrowException() {
        // Given
        String sessionId = "nonexistent";
        when(sessionRepository.findBySessionId(sessionId)).thenReturn(java.util.Optional.empty());

        // Then
        org.junit.jupiter.api.Assertions.assertThrows(
                com.talentradar.user_service.exception.SessionNotFoundException.class,
                () -> sessionService.revokeSessionById(sessionId)
        );

        verify(sessionRepository).findBySessionId(sessionId);
        verify(sessionRepository, never()).deleteBySessionId(anyString());
    }
}
