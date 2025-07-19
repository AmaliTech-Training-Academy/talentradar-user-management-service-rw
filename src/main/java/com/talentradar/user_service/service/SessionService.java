package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.SessionResponseDto;
import com.talentradar.user_service.dto.UserNotFoundException;
import com.talentradar.user_service.exception.InvalidDateFormatException;
import com.talentradar.user_service.exception.SessionNotFoundException;
import com.talentradar.user_service.mapper.SessionMapper;
import com.talentradar.user_service.model.Session;
import com.talentradar.user_service.repository.UserRepository;
import com.talentradar.user_service.repository.UserSessionRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

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
        Page<Session> sessionPage = this.userSessionRepository
                .findAllByIsActiveTrue(pageable);
        logger.info("Admin fetched active sessions");
        return sessionPage.map(sessionMapper::toDto);
    }

    @Transactional
    public void revokeSessionById(String sessionId, HttpSession sessionRequest) {
        if(this.userSessionRepository.findBySessionId(sessionId).isEmpty()){
            throw new SessionNotFoundException(
                    String.format("The session with id '%s' does not exist",
                            sessionId));

        }

        sessionRequest.invalidate(); // this deletes session from Redis
        this.userSessionRepository.deleteBySessionId(sessionId); // this session data in DB
        logger.info("Admin revoked session with ID: {}", sessionId);
    }
    public Page<SessionResponseDto> filterSessions(
            UUID userId,
            String dateString,
            Pageable pageable
    ) {
        logger.info("admin provided, userId= {} and date = {} for filtering",
                userId, dateString);

        // Filter Both userId and date if both are provided
        if (isUserIdValid(userId) && userId != null && dateString != null
            && !dateString.isEmpty()) {
            LocalDate date = convertStringToLocalDate(dateString);
            return filterByUserIdAndDate(userId, date, pageable);
        }

        // Filter only by userId
        if (userId != null) {
            return filterByOnlyUserId(userId, pageable);
        }

        //Filter only by date
        if (dateString != null && !dateString.isEmpty()) {
            LocalDate date = convertStringToLocalDate(dateString);
            return filterByOnlyDate(date, pageable);
        }

        // No filter, return all
        logger.info("Admin fetched with no filter");
        return userSessionRepository
                .findAll(pageable)
                .map(sessionMapper::toDto);

    }

    private boolean isUserIdValid(UUID userId){
        if (userId != null && userRepository.findById(userId).isEmpty()) {
            logger.info("Exception thrown, userId not found!");
            throw new UserNotFoundException(
                    String.format("The user with id '%s' does not exist", userId));
        }
        return true;
    }

    private LocalDate convertStringToLocalDate(String dateString){
            logger.info("Exception thrown, date format is invalid!");
            try {
                return LocalDate.parse(dateString); // Expects format: yyyy-MM-dd
            } catch (DateTimeParseException ex) {
                throw new InvalidDateFormatException("Invalid date format. Expected yyyy-MM-dd " +
                                                     "ex:2025-07-19");
            }
    }

    private Page<SessionResponseDto> filterByUserIdAndDate(UUID userId, LocalDate date,
                                                             Pageable pageable){
        logger.info("Admin filtered by userId and date");

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return userSessionRepository
                .findByUserIdAndCreatedAtBetween(userId, start, end, pageable)
                .map(sessionMapper::toDto);
    }

    private Page<SessionResponseDto> filterByOnlyDate(LocalDate date, Pageable pageable){
        logger.info("Admin filtered only by date");
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(LocalTime.MAX);
        return userSessionRepository
                .findByCreatedAtBetween(start, end, pageable)
                .map(sessionMapper::toDto);
    }

    private Page<SessionResponseDto> filterByOnlyUserId(UUID userId, Pageable pageable){
        logger.info("Admin filtered only by userId");
        return userSessionRepository
                .findAllByUserId(userId, pageable)
                .map(sessionMapper::toDto);
    }
}
