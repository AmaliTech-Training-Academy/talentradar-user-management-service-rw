package com.talentradar.user_service.repository;

import com.talentradar.user_service.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findBySessionId(String sessionId);
    Page<Session> findAllByIsActiveTrue(Pageable pageable);
    void deleteBySessionId(String sessionId);
    Page<Session> findByUserId(String userId, Pageable pageable);
    Page<Session> findByCreatedAt(LocalDateTime dateTime, Pageable pageable);
    Page<Session> findByUserIdAndCreatedAt(String userId, LocalDateTime dateTime, Pageable pageable);
}
