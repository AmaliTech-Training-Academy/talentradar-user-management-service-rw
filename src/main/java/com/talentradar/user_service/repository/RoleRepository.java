package com.talentradar.user_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

import com.talentradar.user_service.model.Role;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByRoleName(String roleName);
}
