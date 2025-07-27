package com.talentradar.user_service.service;

import com.talentradar.user_service.dto.InviteUserRequest;
import com.talentradar.user_service.dto.ResponseDto;
import com.talentradar.user_service.exception.ResourceAlreadyExistsException;
import com.talentradar.user_service.exception.ResourceNotFoundException;
import com.talentradar.user_service.model.Role;
import com.talentradar.user_service.model.User;
import com.talentradar.user_service.repository.RoleRepository;
import com.talentradar.user_service.repository.UserRepository;
import com.talentradar.user_service.service.interfaces.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;
    private final UUID testUserId = UUID.randomUUID();
    private final UUID testRoleId = UUID.randomUUID();
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(testRoleId);
        testRole.setRoleName("USER");

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail(testEmail);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setRole(testRole);
        testUser.setStatus(User.UserStatus.ACTIVE);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("Should get current user successfully")
    void getMe_WithValidUserId_ReturnsUser() {
        // Arrange
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        // Act
        ResponseDto result = userService.getMe(testUserId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("User retrieved successfully", result.getMessage());
        assertNotNull(result.getData());

        verify(userRepository).findById(testUserId);
    }

    @Test
    @DisplayName("Should throw exception when user not found in getMe")
    void getMe_WithInvalidUserId_ThrowsException() {
        // Arrange
        UUID invalidUserId = UUID.randomUUID();
        when(userRepository.findById(invalidUserId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> userService.getMe(invalidUserId));
        verify(userRepository).findById(invalidUserId);
    }

    @Test
    @DisplayName("Should get all users with pagination")
    void getAllUsers_WithValidPagination_ReturnsUsers() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(userRepository.findAll(any(PageRequest.class))).thenReturn(userPage);

        // Act
        ResponseDto result = userService.getAllUsers(1, 10, null);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Users retrieved successfully", result.getMessage());
        assertNotNull(result.getData());

        verify(userRepository).findAll(any(PageRequest.class));
    }

    @Test
    @DisplayName("Should get users filtered by role")
    void getAllUsers_WithRoleFilter_ReturnsFilteredUsers() {
        // Arrange
        Page<User> userPage = new PageImpl<>(List.of(testUser), PageRequest.of(0, 10), 1);
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.of(testRole));
        when(userRepository.findByRole(eq(testRole), any(PageRequest.class))).thenReturn(userPage);

        // Act
        ResponseDto result = userService.getAllUsers(1, 10, testRoleId);

        // Assert
        assertNotNull(result);
        assertTrue(result.getStatus());
        assertEquals("Users retrieved successfully", result.getMessage());

        verify(roleRepository).findById(testRoleId);
        verify(userRepository).findByRole(eq(testRole), any(PageRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when role not found in getAllUsers")
    void getAllUsers_WithInvalidRoleId_ThrowsException() {
        // Arrange
        UUID invalidRoleId = UUID.randomUUID();
        when(roleRepository.findById(invalidRoleId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getAllUsers(1, 10, invalidRoleId));
        verify(roleRepository).findById(invalidRoleId);
    }

    @Test
    @DisplayName("Should throw exception when email already exists")
    void initiateRegistration_WithExistingEmail_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        // Act & Assert
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(testRoleId);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.initiateRegistration(request));
        verify(userRepository).findByEmail(testEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw exception when role not found in initiateRegistration")
    void initiateRegistration_WithInvalidRoleId_ThrowsException() {
        // Arrange
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(roleRepository.findById(testRoleId)).thenReturn(Optional.empty());

        // Act & Assert
        InviteUserRequest request = new InviteUserRequest();
        request.setEmail(testEmail);
        request.setRoleId(testRoleId);

        assertThrows(ResourceNotFoundException.class, () -> userService.initiateRegistration(request));
        verify(userRepository).findByEmail(testEmail);
        verify(roleRepository).findById(testRoleId);
        verify(userRepository, never()).save(any(User.class));
    }
}