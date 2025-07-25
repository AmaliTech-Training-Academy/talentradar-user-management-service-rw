package com.talentradar.user_service.repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.talentradar.user_service.model.Session;

public interface UserSessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findBySessionId(String sessionId);

    Page<Session> findAllByIsActiveTrue(Pageable pageable);

    void deleteBySessionId(String sessionId);

    Page<Session> findAllByUserId(UUID userId, Pageable pageable);

    Page<Session> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end, Pageable pageable);

    Page<Session> findByUserIdAndCreatedAtBetween(UUID userId, LocalDateTime start, LocalDateTime end,
            Pageable pageable);

    Page<Session> findAll(Pageable pageable);
}
