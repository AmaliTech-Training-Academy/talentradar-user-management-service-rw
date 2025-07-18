package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.dto.UserNotFoundException;
import com.talentradar.user_service.exception.SessionNotFoundException;
import com.talentradar.user_service.exception.UnauthorizedException;
import com.talentradar.user_service.mapper.SessionMapper;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.repository.UserRepository;
import com.talentradar.user_service.repository.UserSessionRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {
    private static final Logger logger = LoggerFactory.getLogger(SessionService.class);
    private final UserSessionRepository userSessionRepository;
    private final SessionMapper sessionMapper;
    private final UserRepository userRepository;

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
    public Page<SessionResponseDto> filterSessions(
            UUID userId,
            String stringDate,
            Pageable pageable
    ) {
        Page<Session> sessionList;

        //validate userId
        if (userId != null) {
            if(this.userRepository.findById(userId).isEmpty()){
                throw new UserNotFoundException(
                        String.format("The user with id '%s' does not exist", userId));

            }
        }
        sessionList = userSessionRepository.findAllByUserId(userId,pageable);
        return sessionList.map(sessionMapper::toDto);
    }
}
