package com.talentradar.user_service;

import com.talentradar.user_service.dto.CustomPageResponse;
import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.dto.UserNotFoundException;
import com.talentradar.user_service.exception.InvalidDateFormatException;
import com.talentradar.user_service.mapper.SessionMapper;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.repository.UserRepository;
import com.talentradar.user_service.repository.UserSessionRepository;
import com.talentradar.user_service.service.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.session.SessionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceFilterTest {
    private UserSessionRepository userSessionRepository;
    private SessionMapper sessionMapper;
    private UserRepository userRepository;
    private SessionService sessionService;

    private Pageable pageable;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userSessionRepository = mock(UserSessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        userRepository = mock(UserRepository.class);
        SessionRepository redisSessionRepository = mock(SessionRepository.class);
        sessionService = new SessionService(userSessionRepository, sessionMapper, userRepository, redisSessionRepository);
        pageable = PageRequest.of(0, 10);
        userId = UUID.randomUUID();
    }

    @Test
    void filterSessions_withOnlyUserId_shouldCallFilterByOnlyUserId() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(mock()));

        List<Session> sessions = List.of(new Session());
        when(userSessionRepository.findAllByUserId(userId, pageable)).thenReturn(new PageImpl<>(sessions));
        when(sessionMapper.toDto(any())).thenReturn(mock(SessionResponseDto.class));

        CustomPageResponse<SessionResponseDto> result = sessionService.filterSessions(userId, null, pageable);

        assertEquals(1, result.getItems().size());
        verify(userSessionRepository).findAllByUserId(userId, pageable);
    }

    @Test
    void filterSessions_withOnlyDate_shouldCallFilterByOnlyDate() {
        String dateStr = "2025-07-19";
        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999_999_999);

        List<Session> sessions = List.of(new Session());
        when(userSessionRepository.findByCreatedAtBetween(start, end, pageable)).thenReturn(new PageImpl<>(sessions));
        when(sessionMapper.toDto(any())).thenReturn(mock(SessionResponseDto.class));

        CustomPageResponse<SessionResponseDto> result = sessionService.filterSessions(null, dateStr, pageable);

        assertEquals(1, result.getItems().size());
        verify(userSessionRepository).findByCreatedAtBetween(start, end, pageable);
    }

    @Test
    void filterSessions_withUserIdAndDate_shouldCallFilterByUserIdAndDate() {
        String dateStr = "2025-07-19";
        LocalDate date = LocalDate.parse(dateStr);
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59, 999_999_999);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mock()));
        List<Session> sessions = List.of(new Session());
        when(userSessionRepository.findByUserIdAndCreatedAtBetween(userId, start, end, pageable))
                .thenReturn(new PageImpl<>(sessions));
        when(sessionMapper.toDto(any())).thenReturn(mock(SessionResponseDto.class));

        CustomPageResponse<SessionResponseDto> result = sessionService.filterSessions(userId, dateStr, pageable);

        assertEquals(1, result.getItems().size());
        verify(userSessionRepository).findByUserIdAndCreatedAtBetween(userId, start, end, pageable);
    }

    @Test
    void filterSessions_withNoFilters_shouldReturnAll() {
        List<Session> sessions = List.of(new Session());
        when(userSessionRepository.findAll(pageable)).thenReturn(new PageImpl<>(sessions));
        when(sessionMapper.toDto(any())).thenReturn(mock(SessionResponseDto.class));

        CustomPageResponse<SessionResponseDto> result = sessionService.filterSessions(null, null, pageable);

        assertEquals(1, result.getItems().size());
        verify(userSessionRepository).findAll(pageable);
    }

    @Test
    void filterSessions_withInvalidUserId_shouldThrowException() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () ->
                sessionService.filterSessions(userId, "2025-07-19", pageable));

        verify(userRepository).findById(userId);
    }

    @Test
    void filterSessions_withInvalidDate_shouldThrowException() {
        String invalidDate = "19-07-2025";

        when(userRepository.findById(userId)).thenReturn(Optional.of(mock()));

        assertThrows(InvalidDateFormatException.class, () ->
                sessionService.filterSessions(userId, invalidDate, pageable));
    }
}
