package com.talentradar.user_service.repository;

import com.talentradar.user_service.model.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<Session, UUID> {
    Optional<Session> findBySessionId(String sessionId);
    Page<Session> findAllByIsActiveTrue(Pageable pageable);
}
