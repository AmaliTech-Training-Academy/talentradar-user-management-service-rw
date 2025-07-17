package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.exception.SessionNotFoundException;
import com.talentradar.user_service.mapper.SessionMapper;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.repository.UserSessionRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SessionService {
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    private final UserSessionRepository userSessionRepository;
    private final SessionMapper sessionMapper;

    public Page<SessionResponseDto> getActiveSessions(Pageable pageable) {
        Page<Session> sessionPage = this.userSessionRepository.findAllByIsActiveTrue(pageable);
        logger.info("Admin fetched active sessions");
        return sessionPage.map(sessionMapper::toDto);
    }

    @Transactional
    public void revokeSessionById(String sessionId, HttpSession sessionRequest) {
        if(this.userSessionRepository.findBySessionId(sessionId).isEmpty()){
            throw new SessionNotFoundException(
                    String.format("The session with id email '%s' does not exist", sessionId));

        }

        sessionRequest.invalidate(); // this deletes session from Redis
        this.userSessionRepository.deleteBySessionId(sessionId); // this session data in DB
        logger.info("Admin revoked session with ID: {}", sessionId);
    }
}
