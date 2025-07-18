package com.talentradar.user_service.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);
    Optional<User>findById(UUID userId);
    Page<User> findByRole(Role role, PageRequest pageRequest);

    List<User> findByRole(Role role);
}