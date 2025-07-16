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
        MockitoAnnotations.openMocks(this); // initialize mocks
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
        when(sessionRepository.findAll(any(Pageable.class))).thenReturn(sessionPage);
        when(sessionMapper.toDto(session)).thenReturn(responseDto);
        // When
        Page<SessionResponseDto> result = sessionService.getActiveSessions(PageRequest.of(0, 10));

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.getContent().get(0).getSessionId()).isEqualTo("abc123");

        verify(sessionRepository).findAll(any(Pageable.class));
        verify(sessionMapper).toDto(session);
    }
}
