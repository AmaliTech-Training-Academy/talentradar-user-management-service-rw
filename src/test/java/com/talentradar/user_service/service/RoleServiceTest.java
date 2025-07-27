package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.dto.RoleDto;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleService roleService;

    private UUID testRoleId1;
    private UUID testRoleId2;
    private Role testRole1;
    private Role testRole2;

    @BeforeEach
    void setUp() {
        testRoleId1 = UUID.randomUUID();
        testRoleId2 = UUID.randomUUID();

        testRole1 = new Role();
        testRole1.setId(testRoleId1);
        testRole1.setRoleName("ADMIN");

        testRole2 = new Role();
        testRole2.setId(testRoleId2);
        testRole2.setRoleName("USER");
    }

    @Test
    @DisplayName("Should successfully get all roles")
    void getAllRoles_WithValidRequest_ReturnsAllRoles() {
        // Arrange
        List<Role> roles = List.of(testRole1, testRole2);
        when(roleRepository.findAll()).thenReturn(roles);

        // Act
        ResponseDto result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Roles retrieved successfully", result.getMessage());
        assertNull(result.getErrors());

        Map<String, List<RoleDto>> data = (Map<String, List<RoleDto>>) result.getData();
        List<RoleDto> roleDtos = data.get("roles");

        assertNotNull(roleDtos);
        assertEquals(2, roleDtos.size());

        RoleDto firstRole = roleDtos.get(0);
        assertEquals(testRoleId1, firstRole.getId());
        assertEquals("ADMIN", firstRole.getRoleName());

        RoleDto secondRole = roleDtos.get(1);
        assertEquals(testRoleId2, secondRole.getId());
        assertEquals("USER", secondRole.getRoleName());

        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no roles exist")
    void getAllRoles_WithNoRoles_ReturnsEmptyList() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(List.of());

        // Act
        ResponseDto result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Roles retrieved successfully", result.getMessage());
        assertNull(result.getErrors());

        Map<String, List<RoleDto>> data = (Map<String, List<RoleDto>>) result.getData();
        List<RoleDto> roleDtos = data.get("roles");

        assertNotNull(roleDtos);
        assertTrue(roleDtos.isEmpty());

        verify(roleRepository).findAll();
    }

    @Test
    @DisplayName("Should return single role when only one role exists")
    void getAllRoles_WithSingleRole_ReturnsSingleRole() {
        // Arrange
        when(roleRepository.findAll()).thenReturn(List.of(testRole1));

        // Act
        ResponseDto result = roleService.getAllRoles();

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Roles retrieved successfully", result.getMessage());
        assertNull(result.getErrors());

        Map<String, List<RoleDto>> data = (Map<String, List<RoleDto>>) result.getData();
        List<RoleDto> roleDtos = data.get("roles");

        assertNotNull(roleDtos);
        assertEquals(1, roleDtos.size());

        RoleDto role = roleDtos.get(0);
        assertEquals(testRoleId1, role.getId());
        assertEquals("ADMIN", role.getRoleName());

        verify(roleRepository).findAll();
    }
}