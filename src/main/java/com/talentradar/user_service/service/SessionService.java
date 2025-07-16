package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.mapper.SessionMapper;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.repository.UserSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final UserSessionRepository userSessionRepository;
    private final SessionMapper sessionMapper;

public Page<SessionResponseDto> getActiveSessions(Pageable pageable) {
    Page<Session> sessionPage = this.userSessionRepository.findAll(pageable);

    return sessionPage.map(sessionMapper::toDto);
}
}
