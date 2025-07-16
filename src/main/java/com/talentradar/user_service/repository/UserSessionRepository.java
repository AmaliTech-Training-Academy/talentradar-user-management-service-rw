package com.talentradar.user_service.repository;

import com.talentradar.user_service.model.Session;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSessionRepository extends JpaRepository<Session, Long> {
    //TODO: retrieve or find sessions By userId and session is active
}
